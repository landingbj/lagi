package ai.mr;

import ai.bp.AiGlobalBP;


public interface AiGlobalMR	extends AiGlobalBP	{
	
	// CPU的核数
	public final static int CPU_CORE_COUNTS   = 8;
	public final static boolean GPU_ENABLED   = false;
	
	// MR多线程的实现方式，EXECUTOR代表JDK自带的ExecutorService，RAW代表完全自己的代码控制多线程
	public final static int MR_AS_EXECUTOR   = 1;
	public final static int MR_AS_RAW        = 2;
	public final static int MR_AS_DEFAULT    = MR_AS_EXECUTOR;
	
	public final static int CALC_BY_CPU = 1;
	public final static int CALC_BY_GPU = 2;
	public final static int CALC_BY_DEFAULT = (GPU_ENABLED)?CALC_BY_GPU:CALC_BY_CPU;

	public final static int FAST_DIRECT_PRIORITY 	= 100;
	public final static int FAST_PRIORITY_FACTOR 	= 2;
	public final static int MEDIUM_PRIORITY_FACTOR 	= 3;
	public final static int LAZY_PRIORITY_FACTOR 	= 4;
	public final static int IGNORE_PRIORITY_FACTOR 	= 0;
	
	public final static int M_LIST_RESULT_TEXT 	   = 0;
	public final static int M_LIST_RESULT_PRIORITY = 1;
	
	// 当前模块调试级别
	public static int _DEBUG_LEVEL = _DEBUG_LEVELS[_DEBUG_MR];
}
