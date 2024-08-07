package ai.llm.adapter.impl;

import ai.annotation.LLM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LLM(modelNames = {"vicuna-13b","vicuna-7b","vicuna-7b-16k","vicuna-13B-16k","vicuna-33B"})
public class VicunaAdapter extends OpenAIStandardAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VicunaAdapter.class);
}
