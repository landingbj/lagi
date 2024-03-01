package ai.migrate;

import ai.client.UniServiceCall;
import ai.utils.AiGlobal;

public class DictIndexInitUtils {
	
	private static String _CATEGORY = AiGlobal._CATEGORIES[AiGlobal._CAT_SMARTMP];
	
	private static boolean initUindex  = false;
	private static boolean initZindex  = false;
	private static boolean initSindex  = false;
	
	public static void main(String[] args) {
		
		for(String arg : args)	{
			if(arg.indexOf("-U") != -1)	{
				initUindex = true;
			}
			if(arg.indexOf("-Z") != -1)	{
				initZindex = true;
			}
			if(arg.indexOf("-S") != -1)	{
				initSindex = true;
			}
		}
		
		if(initUindex)	
		{
			String[] dictAliases = UniServiceCall.getDictAlias().split(",");
			
			for (String alias : dictAliases) {
				String result = UniServiceCall.unindexDictInit(alias);
			}
		}
		
		if(initSindex)	
		{
			String alias = "Segment";
			String result = UniServiceCall.unindexDictInit(alias);
		}
		
		if(initZindex)	
		{
			String[] nodeAliases = UniServiceCall.getNodeTableAliasByCategory(_CATEGORY).split(",");
			
			for (String alias : nodeAliases) {
				String result = UniServiceCall.zindexDictInit(alias);
			}
		}
	}
}
