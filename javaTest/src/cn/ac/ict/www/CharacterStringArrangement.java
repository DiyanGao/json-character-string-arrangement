package random;

	import java.io.BufferedReader;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileWriter;
	import java.io.InputStreamReader;


	import com.alibaba.fastjson.JSONArray;
	import com.alibaba.fastjson.JSONObject;

	public class Ctest {

	 public static String formatTime(long ms) {  
	     Integer ss = 1000;  
	     Integer mi = ss * 60;  
	     Integer hh = mi * 60;  
	     Integer dd = hh * 24;  
	   
	     Long hour = (ms) / hh;  
	     Long minute = (ms - hour * hh) / mi;  
	     Long second = (ms - hour * hh - minute * mi) / ss;  
	     Long milliSecond = ms - hour * hh - minute * mi - second * ss;  
	       
	     StringBuffer sb = new StringBuffer();  

	     if(hour > 0) {  
	         sb.append(String.format("%02d", hour)+":");  
	     }else{
	      sb.append("00:");  
	     }
	     
	     if(minute > 0) {  
	         sb.append(String.format("%02d", minute)+":");  
	     }else{
	      sb.append("00:");  
	     }
	     
	     if(second > 0) {  
	         sb.append(String.format("%02d", second)+",");  
	     }else{
	      sb.append("00,");  
	     }
	     
	     if(milliSecond > 0) {  
	         sb.append(String.format("%03d", milliSecond));  
	     }
	     else{
	      sb.append("000");  
	     }
	     return sb.toString();  
	 }  
	 
	 private static JSONArray brailleJsonArray = new JSONArray();
	 private static String original_bg;
	 
	 public static void main(String[] args) {
	  try{
	   File file = new File("/path");//remember to change the path!!
	   InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
	   BufferedReader bufferedReader = new BufferedReader(read);
	   
	   String json_fly = bufferedReader.readLine();
	   
	   JSONArray jsonArray = JSONArray.parseArray(json_fly);//a jsonArray is a array of sentences

	   for (int i = 0; i < jsonArray.size(); i++) {
	    JSONObject jo = jsonArray.getJSONObject(i);//a jo is a sentence
	    String onebest = jo.get("onebest").toString();
	    if (onebest.length() <= 13){
	     brailleJsonArray.add(jo);
	    }
	    else{
	     JSONArray ja_words = jo.getJSONArray("wordsResultList"); //jo analysis (sentence fragments)
	     original_bg = jo.get("bg").toString();
	     cutLine(ja_words);//cut line into parts (could be >two parts)
	       }
	      }
	   
	   String out_json = brailleJsonArray.toJSONString();
	   System.out.println(out_json);

	   try {
	    File file_out = new File("/path");//remember to change the path!!
	    FileWriter fileWriter = new FileWriter(file_out);
	    
	    JSONArray jsonArray2 = JSONArray.parseArray(out_json);
	    int size2 = jsonArray2.size();
	    for (int i = 0; i < size2; i++) {
	     JSONObject jo = jsonArray2.getJSONObject(i);
	     String bg = jo.get("bg").toString();
	     String ed = jo.get("ed").toString();
	     String onebest = jo.get("onebest").toString();

	     Integer bg_time = Integer.parseInt(bg);
	     Integer ed_time = Integer.parseInt(ed);

	     fileWriter.write(String.valueOf(i + 1));
	     fileWriter.write("\r\n" + formatTime(bg_time) + " --> "
	       + formatTime(ed_time) + "\r\n");

	     char[] words = onebest.toCharArray();
	     for (int j = 0; j < words.length; j++) {
	       fileWriter.write(words[j]);
	     }
	     fileWriter.write("\r\n\r\n");
	    }
	    fileWriter.close();
	   } catch (Exception e) {
	    e.printStackTrace();
	   }
	  }
	   
	  catch(Exception e){
	   e.printStackTrace();
	  }

	  
	 }//end main

	 //methods for cutting a line
	 public static void cutLine(JSONArray jawords){
	   if(count(jawords)%13==0||count(jawords)<13){//base-case
	     int thresh=count(jawords)/13;
	     JSONArray theLeftOver = rCutLine(jawords);
	     thresh--;
	    while(thresh>0){
	       theLeftOver = rCutLine(theLeftOver);
	       thresh--;
	    }}//end if
	   else{//count(jawords)%13!=0 && count(jawords)>13
	     int thresh=count(jawords)/13;//only will thresh>=1 appear
	    if(thresh==1) {averageCut(jawords);}
	     else {//if thresh>1
	    	     JSONArray theLeftOver = rCutLine(jawords);
	    	     thresh=count(theLeftOver)/13;
	    	    while(thresh>1){
	    	      theLeftOver = rCutLine(theLeftOver);
	    	       thresh=count(theLeftOver)/13;}
	   averageCut(theLeftOver);}
	    }//end of else
	 }//end of cutLine
	    
	 //cut&input
	 public static void addInBJsA(String headBg, String tailEd, String oneBestWords, JSONArray jsarr){
	   
	    JSONObject jo_words_part = new JSONObject();
	       jo_words_part.put("bg", headBg);
	       int part_ed = Integer.parseInt(headBg) + Integer.parseInt(tailEd)*10;
	       jo_words_part.put("ed", part_ed);
	       original_bg = String.valueOf(part_ed);
	       jo_words_part.put("onebest", oneBestWords);
	       jo_words_part.put("wordsResultList", jsarr);
	       
	       brailleJsonArray.add(jo_words_part.clone());
	 }
	 
	 //helper method
	 public static JSONArray rCutLine(JSONArray jsarr){
	    JSONArray ja_brailleWords = new JSONArray();
	    if(count(jsarr)<=13) {
	    String onebest_words= "";//initiate a part of the whole line
	       for(int k = 0; k < jsarr.size(); k++){
	        onebest_words+= jsarr.getJSONObject(k).get("wordsName").toString();
	        ja_brailleWords.add(jsarr.getJSONObject(k));
	       }
	       
	      JSONObject head=jsarr.getJSONObject(0);
	      String head_bg=head.get("wordBg").toString();
	      head_bg=String.valueOf((Integer.valueOf(head_bg))+Integer.valueOf(original_bg));
	      JSONObject tail=jsarr.getJSONObject(jsarr.size()-1);
	      String tail_ed=tail.get("wordEd").toString();
	      
	      //String head_bg, String tail_ed, String onebestWords, ja_b
	      addInBJsA(head_bg, tail_ed, onebest_words,ja_brailleWords);
	      return jsarr;}
	    //count(jsarr)>13
	    	String onebest_words= "";//initiate a part of the whole line
	        for(int k = 0; k < remainIndex(jsarr); k++){
	         onebest_words+= jsarr.getJSONObject(k).get("wordsName").toString();
	         ja_brailleWords.add(jsarr.getJSONObject(k));
	        }
	        
	       JSONObject head=jsarr.getJSONObject(0);
	       String head_bg=head.get("wordBg").toString();
	       head_bg=String.valueOf((Integer.valueOf(head_bg))+Integer.valueOf(original_bg));
	       JSONObject tail=jsarr.getJSONObject(remainIndex(jsarr)-1);
	       String tail_ed=tail.get("wordEd").toString();
	       
	       //String head_bg, String tail_ed, String onebestWords, ja_b
	       addInBJsA(head_bg, tail_ed, onebest_words,ja_brailleWords);
	       
	       JSONArray left=new JSONArray();
	       for(int i=remainIndex(jsarr); i<jsarr.size(); i++)
	    	   left.add(jsarr.getJSONObject(i));
	       
	       return left;
	 }//end of the method

	//averageCut
	public static void averageCut(JSONArray jsarr){
	  //part 1
	   String onebest_Words= "";
	    JSONArray ja_brailleWords = new JSONArray();
	  for(int k = 0; k< jsarr.size()/2; k++){
	    onebest_Words+=jsarr.getJSONObject(k).get("wordsName").toString();
	    ja_brailleWords.add(jsarr.getJSONObject(k));
	  }
	  
	   JSONObject head=jsarr.getJSONObject(0);
	   JSONObject tail=jsarr.getJSONObject(jsarr.size()/2-1);
	      String head_bg=head.get("wordBg").toString();
	      int x=Integer.valueOf(head_bg).intValue();
	      int y=Integer.valueOf(original_bg).intValue();
	      head_bg=String.valueOf(x+y);
	      String tail_ed=tail.get("wordEd").toString();
	      
	      //String head_bg, String tail_ed, String onebestWords
	      addInBJsA(head_bg, tail_ed, onebest_Words, ja_brailleWords);
	      
	   //part 2
	       String onebest_Words_p2= "";
	      JSONArray ja_brailleWords_p2 = new JSONArray();
	  for(int k = jsarr.size()/2; k< jsarr.size(); k++){
	    onebest_Words_p2+=jsarr.getJSONObject(k).get("wordsName").toString();
	    ja_brailleWords_p2.add(jsarr.getJSONObject(k));
	  }
	  
	   JSONObject head_p2=jsarr.getJSONObject(jsarr.size()/2);
	   JSONObject tail_p2=jsarr.getJSONObject(jsarr.size()-1);
	      String head_bg_p2=head_p2.get("wordBg").toString();
	      int x_p2=Integer.valueOf(head_bg_p2).intValue();
	      int y_p2=Integer.valueOf(original_bg).intValue();
	      head_bg_p2=String.valueOf(x_p2+y_p2);
	      String tail_ed_p2=tail_p2.get("wordEd").toString();
	      
	      //String head_bg, String tail_ed, String onebestWords
	      addInBJsA(head_bg_p2, tail_ed_p2, onebest_Words_p2, ja_brailleWords_p2);
	      
	   }//end method

	//countMethod
	public static int count(JSONArray jsarr){
	  int countSum=0;
	  String wordsName="";
	      for (int index=0; index<jsarr.size(); index++){//count numbers of the fragments in a sentence
	      JSONObject jo_word = jsarr.getJSONObject(index);//assign each fragment to jo_word
	      wordsName = jo_word.get("wordsName").toString();
	       countSum += wordsName.length();}//calculate the sum charcs of a ja_words
	      
	      return countSum;
	}

	//remainIndex method
	public static int remainIndex (JSONArray jsarr) {
		int sum=0;
		int index=0;
		String wordsName="";
		while(sum<=13) {
			JSONObject jo_one = jsarr.getJSONObject(index);
			wordsName =jo_one.get("wordsName").toString();
			sum+=wordsName.length();
			index++;}
		return index;
	}
	}//end of the class

