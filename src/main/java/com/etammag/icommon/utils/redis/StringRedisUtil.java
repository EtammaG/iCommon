package com.etammag.icommon.utils.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StringRedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    public void delete(String key) {
        stringRedisTemplate.unlink(key);
    }

    public void deleteAll(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) stringRedisTemplate.unlink(keys);
    }

    public StringRedisUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public <V> V tryValue(
            Supplier<V> db,
            String key,
            //Duration duration,
            Function<V, String> v2s,
            Function<String, V> s2v) {
        String str = stringRedisTemplate.opsForValue().get(key);
        if (str != null) return str.isEmpty() ? null : s2v.apply(str);
        V v = db.get();
        //if (v == null) stringRedisTemplate.opsForValue().set(key, "", duration);
        //else stringRedisTemplate.opsForValue().set(key, v2s.apply(v), duration);
        if (v == null) stringRedisTemplate.opsForValue().set(key, "");
        else stringRedisTemplate.opsForValue().set(key, v2s.apply(v));
        return v;
    }

    public <V> boolean catValue(
            Supplier<V> db,
            String key,
            //Duration duration,
            Function<V, String> v2s) {
        String str = stringRedisTemplate.opsForValue().get(key);
        if (str != null) return !str.isEmpty();
        V v = db.get();
        if (v == null) {
            //stringRedisTemplate.opsForValue().set(key, "", duration);
            stringRedisTemplate.opsForValue().set(key, "");
            return false;
        }
        //stringRedisTemplate.opsForValue().set(key, v2s.apply(v), duration);
        stringRedisTemplate.opsForValue().set(key, v2s.apply(v));
        return true;
    }

    public <V> List<V> tryAllValue(
            Function<List<Integer>, List<V>> db,
            List<String> keys,
            //Duration duration,
            Function<V, String> v2s,
            BiFunction<Integer, String, V> s2v) {
        List<String> strs = stringRedisTemplate.opsForValue().multiGet(keys);
        List<Integer> needs;
        List<V> res = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) res.add(null);
        if (strs == null) {
            needs = new ArrayList<>(keys.size());
            for (int index = 0; index < keys.size(); index++) needs.add(index);
        } else {
            needs = new ArrayList<>();
            for (int index = 0; index < strs.size(); index++) {
                String str = strs.get(index);
                if (str == null) needs.add(index);
                else if (!str.isEmpty()) res.set(index, s2v.apply(index, str));
            }
        }
        if (!needs.isEmpty()) {
            List<V> patches = db.apply(needs);
            Map<String, String> toRedis = new HashMap<>(patches.size());
            for (int i = 0, j = 0; i < res.size(); i++) {
                if (res.get(i) == null) {
                    res.set(i, patches.get(j++));
                    toRedis.put(keys.get(i), v2s.apply(res.get(i)));
                }
            }
            stringRedisTemplate.opsForValue().multiSet(toRedis);
        }
        return res;
    }

    public void deleteValue(String key) {
        this.delete(key);
    }

    public <V> List<V> tryList(
            Supplier<List<V>> db,
            String key,
            //Duration duration,
            Function<V, String> v2s,
            Function<String, V> s2v) {
        // 使用另一个value类型实现缓存空对象解决缓存穿透
        String emptyKey = key + ":empty";
        if (stringRedisTemplate.opsForValue().get(emptyKey) != null) return new LinkedList<>();
        Long size = stringRedisTemplate.opsForList().size(key);
        if (size != null && size != 0) {
            List<String> strs = stringRedisTemplate.opsForList().range(key, 0, size);
            if (strs == null || strs.isEmpty()) return new LinkedList<>();
            return strs.stream().map(s2v).collect(Collectors.toList());
        }
        List<V> value = db.get();
        if (value == null || value.isEmpty()) {
            //stringRedisTemplate.opsForValue().set(emptyKey, "", duration);
            stringRedisTemplate.opsForValue().set(emptyKey, "");
            return new LinkedList<>();
        }
        stringRedisTemplate.opsForList().rightPushAll(key, value.stream().map(v2s).collect(Collectors.toList()));
        return value;
    }

    public void deleteList(String key) {
        this.delete(key);
        this.delete(key + ":empty");
    }

    public void deleteAllList(String pattern) {
        this.deleteAll(pattern);
    }

    public <V> V tryHashValue(
            Supplier<V> db,
            String key,
            String field,
            //Duration duration,
            Function<V, String> v2s,
            Function<String, V> s2v) {
        String str = (String) stringRedisTemplate.opsForHash().get(key, field);
        if (str != null) return str.isEmpty() ? null : s2v.apply(str);
        V value = db.get();
        if (value == null) stringRedisTemplate.opsForHash().put(key, field, "");
        else stringRedisTemplate.opsForHash().put(key, field, v2s.apply(value));
        //stringRedisTemplate.expire(key, duration);
        return value;
    }

    public <V> boolean catHashValue(
            Supplier<V> db,
            String key,
            String field,
            //Duration duration,
            Function<V, String> v2s) {
        String str = (String) stringRedisTemplate.opsForHash().get(key, field);
        if (str != null) return !str.isEmpty();
        V value = db.get();
        if (value == null) {
            stringRedisTemplate.opsForHash().put(key, field, "");
            //stringRedisTemplate.expire(key, duration);
            return false;
        }
        stringRedisTemplate.opsForHash().put(key, field, v2s.apply(value));
        //stringRedisTemplate.expire(key, duration);
        return true;
    }

    public <K, V> Map<K, V> tryHash(
            Supplier<Map<K, V>> db,
            String key,
            //Duration duration,
            Function<V, String> v2s,
            BiFunction<K, String, V> s2v,
            Function<K, String> k2s,
            Function<String, K> s2k) {
        String flagKey = key + ":flag";
        String flag = stringRedisTemplate.opsForValue().get(flagKey);
        // "" means 空   “*” means 数据完整  null means 未缓存
        if (flag != null && flag.isEmpty()) return new HashMap<>(0);
        if (flag != null) {
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            Map<K, V> res = new HashMap<>(entries.size());
            for (Map.Entry<Object, Object> e : entries.entrySet()) {
                K k = s2k.apply((String) e.getKey());
                V v = s2v.apply(k, (String) e.getValue());
                res.put(k, v);
            }
            return res;
        }
        Map<K, V> res = db.get();
        if (res == null || res.isEmpty()) {
            //stringRedisTemplate.opsForValue().set(flagKey, "", duration);
            stringRedisTemplate.opsForValue().set(flagKey, "");
            return new HashMap<>(0);
        }
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<K, V> e : res.entrySet())
            map.put(k2s.apply(e.getKey()), v2s.apply(e.getValue()));
        stringRedisTemplate.opsForValue().set(flagKey, "1");
        stringRedisTemplate.opsForHash().putAll(key, map);
        //stringRedisTemplate.expire(key, duration);
        return res;
    }

    public <K, V> Set<K> tryHashKey(
            Supplier<Map<K, V>> db,
            String key,
            Function<V, String> v2s,
            Function<K, String> k2s,
            Function<String, K> s2k) {
        boolean exist = this.catHash(db, key, v2s, k2s);
        if(!exist) return new HashSet<>(0);
        return stringRedisTemplate.opsForHash().keys(key)
                .stream().map((s) -> s2k.apply((String)s)).collect(Collectors.toSet());
    }

    public <K, V> boolean catHash(
            Supplier<Map<K, V>> db,
            String key,
            //Duration duration,
            Function<V, String> v2s,
            Function<K, String> k2s) {
        String flagKey = key + ":flag";
        String flag = stringRedisTemplate.opsForValue().get(flagKey);
        if(flag != null && flag.isEmpty()) return false;
        if(flag != null) return true;
        Map<K, V> res = db.get();
        if(res == null || res.isEmpty()) {
            //stringRedisTemplate.opsForValue().set(flagKey, "", duration);
            stringRedisTemplate.opsForValue().set(flagKey, "");
            return false;
        }
        Map<String, String> map = new HashMap<>();
        for(Map.Entry<K, V> e : res.entrySet())
            map.put(k2s.apply(e.getKey()), v2s.apply(e.getValue()));
        stringRedisTemplate.opsForValue().set(flagKey, "1");
        stringRedisTemplate.opsForHash().putAll(key, map);
        return true;
    }

    public void deleteHashValue(String key, String... field) {
        stringRedisTemplate.opsForHash().delete(key, (Object[]) field);
    }

    public void deleteHash(String key) {
        this.delete(key);
        this.delete(key + ":flag");
    }

    public <V> Set<V> trySet(
            Supplier<Set<V>> db,
            String key,
            //Duration duration,
            Function<V, String> v2s,
            Function<String, V> s2v) {
        String flagKey = key + ":flag";
        String flag = stringRedisTemplate.opsForValue().get(flagKey);
        // "" means 空   “*” means 数据完整  null means 未缓存
        if (flag != null && flag.isEmpty()) return new HashSet<>(0);
        if (flag != null) {
            Set<String> strs = stringRedisTemplate.opsForSet().members(key);
            if (strs == null || strs.isEmpty()) return new HashSet<>(0);
            return strs.stream().map(s2v).collect(Collectors.toSet());
        }
        Set<V> res = db.get();
        if (res == null || res.isEmpty()) {
            //stringRedisTemplate.opsForValue().set(flagKey, "", duration);
            stringRedisTemplate.opsForValue().set(flagKey, "");
            return new HashSet<>(0);
        }
        stringRedisTemplate.opsForValue().set(flagKey, "1");
        stringRedisTemplate.opsForSet().add(key, res.stream().map(v2s).toArray(String[]::new));
        //stringRedisTemplate.expire(key, duration);
        return res;
    }

    public <V> boolean catSet(
            Supplier<Set<V>> db,
            String key,
            //Duration duration,
            Function<V, String> v2s) {
        String flagKey = key + ":flag";
        String flag = stringRedisTemplate.opsForValue().get(flagKey);
        if (flag != null && flag.isEmpty()) return false;
        if (flag != null) return true;
        Set<V> res = db.get();
        if (res == null || res.isEmpty()) {
            //stringRedisTemplate.opsForValue().set(flagKey, "", duration);
            stringRedisTemplate.opsForValue().set(flagKey, "");
            return false;
        }
        stringRedisTemplate.opsForValue().set(flagKey, "1");
        stringRedisTemplate.opsForSet().add(key, res.stream().map(v2s).toArray(String[]::new));
        //stringRedisTemplate.expire(key, duration);
        return true;
    }

    public void deleteSet(String key) {
        this.delete(key);
        this.delete(key + ":flag");
    }

}
