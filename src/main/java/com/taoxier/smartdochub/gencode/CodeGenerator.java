package com.taoxier.smartdochub.gencode;

/**
 * @Author taoxier
 * @Date 2025/10/14 下午8:53
 * @描述
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    // 数据库连接信息
    private static final String URL = "jdbc:mysql://localhost:3306/smartdochub?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    // 项目基础包路径
    private static final String BASE_PACKAGE = "com.taoxier.smartdochub";
    // 输出目录(根据实际项目路径修改)
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/src/main/java";
    // Mapper.xml输出目录
    private static final String MAPPER_XML_OUTPUT_DIR = System.getProperty("user.dir") + "/src/main/resources/mapper";

    public static void main(String[] args) {
        // 数据库表名，可根据需要修改
        String[] tables = {
                "doc_content_chunk",
                "doc_document",
                "doc_kg_mapping",
                "doc_similarity",
                "doc_topic"
        };

        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("taoxier") // 设置作者
                            .outputDir(OUTPUT_DIR) // 指定输出目录
                            .commentDate("yyyy-MM-dd") // 注释日期格式
                            .disableOpenDir(); // 生成后不打开文件夹
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(BASE_PACKAGE) // 父包名
                            .moduleName("document") // 模块名，根据实际模块修改
                            .entity("model") // 实体类包名
                            .mapper("mapper") // mapper接口包名
                            .service("service") // service接口包名
                            .serviceImpl("service.impl") // service实现类包名
                            .controller("controller") // controller包名
                            .xml("mapper") // mapper.xml包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, MAPPER_XML_OUTPUT_DIR)); // 设置mapper.xml输出路径
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(tables) // 设置需要生成的表名
                            .addTablePrefix("t_", "sys_","doc_","kg_") // 设置表前缀，生成类名时会去掉前缀

                            // 实体类策略配置
                            .entityBuilder()
                            .enableLombok() // 开启Lombok
                            .enableTableFieldAnnotation() // 开启字段注解
                            .logicDeleteColumnName("deleted") // 逻辑删除字段
                            .versionColumnName("version") // 乐观锁字段

                            // Controller策略配置
                            .controllerBuilder()
                            .enableRestStyle() // 开启Rest风格
                            .enableHyphenStyle() // 开启连字符命名风格

                            // Service策略配置
                            .serviceBuilder()
                            .formatServiceFileName("%sService") // Service接口命名格式
                            .formatServiceImplFileName("%sServiceImpl") // Service实现类命名格式

                            // Mapper策略配置
                            .mapperBuilder()
                            .superClass(BaseMapper.class) // 设置父类
                            .enableBaseResultMap() // 开启BaseResultMap
                            .enableBaseColumnList(); // 开启BaseColumnList
                })
                // 模板引擎配置
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行生成
                .execute();
    }
}

