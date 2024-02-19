package com.etammag.icommon.mbpg;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class PlusGenerator {

    private static final String dbUrl = "jdbc:mysql://192.168.200.100:3306/dream_lighter";
    private static final String moduleName = "ieat";
    private static final String outputDir = "D:\\CodeProject\\IntelliJ_IDEA\\" + "iEat" + "\\src\\main";

    public static void main(String[] args) {
        FastAutoGenerator.create(
                        dbUrl,
                        "root", "Root_123")
                .globalConfig(builder -> {
                    builder
                            .disableOpenDir()
                            //.enableSwagger()
                            .outputDir(outputDir + "\\java")
                    ;
                })
                .packageConfig(builder -> {
                    builder.parent("com.etammag") // 设置父包名
                            .moduleName(moduleName) // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, outputDir + "\\resources\\mapper")) // 设置mapperXml生成路径
                    ;
                })
                .strategyConfig(builder -> {
                    builder// 设置需要生成的表名
                            .addInclude("volun_article_love")
                            .addTablePrefix("volun_")
                            .entityBuilder()
                            .enableLombok() //使用lombok
                            .disableSerialVersionUID()
                            //.enableTableFieldAnnotation()
//                                    .addTableFills(Arrays.asList(
//                                            new Column("create_time", FieldFill.INSERT),
//                                            new Column("create_user", FieldFill.INSERT),
//                                            new Column("update_time", FieldFill.INSERT_UPDATE),
//                                            new Column("update_user", FieldFill.INSERT_UPDATE)
//                                    ))
//                                .logicDeleteColumnName("is_deleted")
//                            .fileOverride()
                            .controllerBuilder()
                            .enableRestStyle()
                            //.fileOverride()
                            .mapperBuilder()
                            .enableMapperAnnotation()
                            .enableBaseResultMap()
                            //.enableBaseColumnList()
                            .fileOverride()
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                    //.fileOverride()
                    ;
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

}