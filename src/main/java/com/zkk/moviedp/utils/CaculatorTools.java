package com.zkk.moviedp.utils;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

@Component
public class CaculatorTools {

    @Tool( name = "Additive_operations", value = "Add the two parameters together to return the result.")
    double sum(@ToolMemoryId int memoryId, double a, double b){
        System.out.println("调用sum工具 memoryId" + memoryId);
        return a + b;
    }
}
