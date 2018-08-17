package cn.ac.ict.www;

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

	public static void main(String[] args) {
		try{
			File file = new File("C:\\spring.json");
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");// 考虑到编码格式,文件是以utf-8编码的
			BufferedReader bufferedReader = new BufferedReader(read);
			
			String json_fly = bufferedReader.readLine();
			JSONArray jsonArray = JSONArray.parseArray(json_fly);
			JSONArray brailleJsonArray = new JSONArray();
			int size = jsonArray.size();
			for (int i = 0; i < size; i++) {
				JSONObject jo = jsonArray.getJSONObject(i);
				String onebest = jo.get("onebest").toString();
				if (onebest.length() <= 13){
					brailleJsonArray.add(jo);
				}else{
					JSONArray ja_words = jo.getJSONArray("wordsResultList");
					JSONArray ja_brailleWords = new JSONArray();
					String original_bg = jo.get("bg").toString();
					String original_ed = jo.get("ed").toString();
					int currWordsCount = 0;
					for(int j = 0; j < ja_words.size(); j++){
						JSONObject jo_word = ja_words.getJSONObject(j);

						String wordsName = jo_word.get("wordsName").toString();
						int wordsLen = wordsName.length();
						if ((currWordsCount + wordsLen) > 13){
							String bg_words = ja_brailleWords.getJSONObject(0).get("wordBg").toString();
							String ed_words = ja_brailleWords.getJSONObject(ja_brailleWords.size() - 1).get("wordEd").toString();
							String onebest_words = "";
							for(int k = 0; k < ja_brailleWords.size(); k++){
								onebest_words += ja_brailleWords.getJSONObject(k).get("wordsName").toString();
							}
							JSONObject jo_words_part = new JSONObject();
							jo_words_part.put("bg", original_bg);
							int part_ed = Integer.parseInt(original_bg) + Integer.parseInt(ed_words)*10;
							jo_words_part.put("ed", part_ed);
							original_bg = String.valueOf(part_ed);
							jo_words_part.put("onebest", onebest_words);
							jo_words_part.put("wordsResultList", ja_brailleWords.clone());
							
							brailleJsonArray.add(jo_words_part.clone());
							
							ja_brailleWords.clear();
							//TODO:将jo_word中的wordBg,wordEd更新为新
							ja_brailleWords.add(jo_word);
							currWordsCount = wordsLen;
						}else{
							currWordsCount += wordsLen;
							ja_brailleWords.add(jo_word);
						}
						
					}
					
					//将剩余的词填入json数组中
					String onebest_words = "";
					for(int k = 0; k < ja_brailleWords.size(); k++){
						onebest_words += ja_brailleWords.getJSONObject(k).get("wordsName").toString();
					}
					JSONObject jo_words_part = new JSONObject();
					jo_words_part.put("bg", original_bg);
					jo_words_part.put("ed", original_ed);
					jo_words_part.put("onebest", onebest_words);
					jo_words_part.put("wordsResultList", ja_brailleWords);
					
					brailleJsonArray.add(jo_words_part.clone());
					
				}
				
			}
			
			//转化为json字符串
			String out_json = brailleJsonArray.toJSONString();
			System.out.println(out_json);
			
			//转写为srt字幕
			try {
				File file_out = new File("C:\\srt.srt");
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
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
