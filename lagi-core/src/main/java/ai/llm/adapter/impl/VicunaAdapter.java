package ai.llm.adapter.impl;

import ai.annotation.LLM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@LLM(modelNames = {"vicuna-13b","vicuna-7b","vicuna-7b-16k","vicuna-13B-16k","vicuna-33B"})
@LLM(modelNames = {"DeepSeek-R1","智谱Edge-1.5B","拉马3.2-1B","智谱4-9B","DeepSeek-R1-8B","DeepSeek-R1-7B"})
public class VicunaAdapter extends OpenAIStandardAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VicunaAdapter.class);
}
