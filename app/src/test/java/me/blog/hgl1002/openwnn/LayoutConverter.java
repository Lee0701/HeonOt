package me.blog.hgl1002.openwnn;

import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.blog.hgl1002.openwnn.KOKR.EngineMode;

public class LayoutConverter {

	EngineMode engineMode;

	public LayoutConverter(EngineMode engineMode) {
		this.engineMode = engineMode;
	}

	public String convert() {
		try {
			JSONObject layout = new JSONObject();
			layout.put("direct", engineMode.properties.direct);
			layout.put("timeout", engineMode.properties.timeout);
			layout.put("fullMoachigi", engineMode.properties.fullMoachigi);

			/*
			if(engineMode.jamoset != null) {
				JSONArray table = new JSONArray();
				for(int[] primaryItem : engineMode.jamoset[0]) {
					JSONObject map = new JSONObject();
					map.put("keycode", primaryItem[0]);
					JSONArray codes = new JSONArray();
					for(int[][] jamoTable : engineMode.jamoset) {
						for(int[] item : jamoTable) {
							if(item[0] == primaryItem[0]) {
								JSONObject entry = new JSONObject();
								JSONArray arr = new JSONArray();
								arr.put(item[1]);
								entry.put("normal", arr);
								arr = new JSONArray();
								arr.put(item[2]);
								entry.put("shift", arr);
								codes.put(entry);
							}
						}
					}
					map.put("codes", codes);
					table.put(map);
				}
				layout.put("table", table);
			} else */if(engineMode.layout != null) {
				JSONArray table = new JSONArray();
				for(int[] item : engineMode.layout) {
					JSONObject map = new JSONObject();
					map.put("keycode", convertCode(item[0]));
					map.put("normal", Integer.toString(item[1]));
					map.put("shift", Integer.toString(item[2]));
					table.put(map);
				}
				layout.put("table", table);
			}

			if(engineMode.combination != null) {
				JSONArray combination = new JSONArray();
				for(int[] item : engineMode.combination) {
					JSONObject comb = new JSONObject();
					comb.put("a", item[0]);
					comb.put("b", item[1]);
					comb.put("result", Integer.toString(item[2]));
					combination.put(comb);
				}
				layout.put("combination", combination);
			}

			/*
			if(engineMode.virtual != null) {
				JSONArray virtual = new JSONArray();
				for(int[] item : engineMode.virtual) {
					JSONObject v = new JSONObject();
					v.put("a", item[0]);
					v.put("result", item[1]);
					virtual.put(v);
				}
				layout.put("virtual", virtual);
			}
			*/
			return layout.toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int convertCode(int code) {
		if(code >= 0x30 && code <= 0x39) return code - 41;
		if(code >= 0x61 && code <= 0x7a) return code - 68;
		switch(code) {
		case 44: return 55;
		case 46: return 56;
		case 32: return 62;
		case 96: return 68;
		case 45: return 69;
		case 61: return 70;
		case 91: return 71;
		case 93: return 72;
		case 92: return 73;
		case 59: return 74;
		case 39: return 75;
		case 47: return 76;
		}
		return code;
	}

}
