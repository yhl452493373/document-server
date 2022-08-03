package com.optima.document.server.utils;



import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xianjun on 2016/6/17.
 */

/**
 * 转型操作工具类
 */
public final class CastUtil {

	/**
	 * 转为String 型 默认为空
	 */
	public static String castString(Object obj) {
		return CastUtil.castString(obj, "");
	}

	/**
	 * 转为String 型(提供默认值)
	 */
	public static String castString(Object obj, String defaultValue) {
		return obj != null ? String.valueOf(obj) : defaultValue;
	}

	/**
	 * 转为double型 默认为0
	 */
	public static double castDouble(Object obj) {
		return CastUtil.castDouble(obj, 0);
	}

	/**
	 * 转为double型(提供默认值)
	 */
	public static double castDouble(Object obj, double defaultValue) {
		double doubleValue = defaultValue;
		if (obj != null) {
			String strValue = castString(obj);
			if (StringUtils.isNotEmpty(strValue)) {
				try {
					doubleValue = Double.parseDouble(strValue);
				} catch (NumberFormatException e) {
					doubleValue = defaultValue;
				}
			}
		}
		return doubleValue;
	}

	/**
	 * 转为long型,默认值为0
	 */
	public static long castLong(Object obj) {
		return CastUtil.castLong(obj, 0);
	}

	/**
	 * 转换为long型(提供默认值)
	 */
	public static long castLong(Object obj, long defaultValue) {
		long longValue = defaultValue;
		if (obj != null) {
			String strValue = castString(obj);
			if (!StringUtils.isEmpty(strValue)) {
				try {
					longValue = Long.parseLong(strValue);
				} catch (NumberFormatException e) {
					longValue = defaultValue;
				}
			}
		}
		return longValue;
	}

	/**
	 * 转为int型 默认值为0
	 */
	public static int castInt(Object obj) {
		return CastUtil.castInt(obj, 0);
	}

	/**
	 * 转为int型(提供默认值)
	 */
	public static int castInt(Object obj, int defaultValue) {
		int intValue = defaultValue;
		if (obj != null) {
			String strValue = castString(obj);
			if (StringUtils.isNotEmpty(strValue)) {
				try {
					intValue = Integer.parseInt(strValue);
				} catch (NumberFormatException e) {
					intValue = defaultValue;
				}
			}
		}
		return intValue;
	}

	/**
	 * 转为double型，默认值为false
	 */
	public static boolean castBoolean(Object obj) {
		return CastUtil.castBoolean(obj, false);
	}

	/**
	 * 转为double型，提供默认值
	 */
	public static boolean castBoolean(Object obj, boolean defaultValue) {
		boolean booleanValue = defaultValue;
		if (obj != null) {
			booleanValue = Boolean.parseBoolean(castString(obj));
		}
		return booleanValue;
	}

	/**
	 * 转时间戳为date类型
	 * 
	 * @param time
	 * @param defaultValue
	 * @return
	 */
	public static Date castDate(long time, Date defaultValue) {
		Date dateValue = defaultValue;
		if (time != 0) {
			DateTime dateTime = new DateTime(time);
			dateValue = dateTime.toDate();
		}

		return dateValue;
	}

	public static Date castDateNo(long time) {
		return castDate(time, new Date(0));
	}
	
	/**
	 * 转时间戳为date类型
	 */
	public static Date castDate(long time) {
		return castDate(time, new Date());
	}

	/**
	 * 获取开始时间
	 * 
	 * @Title: getStartTime  
	 * @author: xianjun
	 * @Description: TODO  
	 * @param date
	 * @param monthTime
	 * @param dateTime
	 * @param hour
	 * @return
	 * @throws
	 */
	public static Date getStartTime(Date date, int monthTime, int dateTime, int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(CastUtil.castInt(CastUtil.castStringByDate(date, "yyyy")), date.getMonth() + monthTime, date.getDate() + dateTime, 0 + hour, 0, 0);
		return calendar.getTime();
	}
	
	/**
	 * 获取结束时间
	 * 
	 * @Title: getEndTime  
	 * @author: xianjun
	 * @Description: TODO  
	 * @param date
	 * @param monthTime
	 * @param dateTime
	 * @param hour
	 * @return
	 * @throws
	 */
	public static Date getEndTime(Date date, int monthTime, int dateTime, int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(CastUtil.castInt(CastUtil.castStringByDate(date, "yyyy")), date.getMonth() + monthTime, date.getDate() + dateTime, 23 + hour, 59, 59);
		return calendar.getTime();
	}
	
	/**
	 * 转日期格式为字符串
	 */
	public static String castStringDate(Date date, Date defaultValue) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateValue = defaultValue;
		String dateString = "";
		if (date != null) {
			dateString = fmt.format(date);
		} else {
			dateString = fmt.format(dateValue);
		}

		return dateString;
	}

	/**
	 * 转换日期格式为年-月-日
	 * @Title: castStringIntegerDate  
	 * @author: xianjun
	 * @Description: TODO  
	 * @param date
	 * @return
	 * @throws
	 */
	public static String castStringIntegerDate(Date date) {
		return castStringIntegerDate(date, new Date());
	}
	
	/**
	 * 转换格式为年-月-日
	 * @Title: castStringIntegerDate  
	 * @author: xianjun
	 * @Description: TODO  
	 * @param date
	 * @param defaultValue
	 * @return
	 * @throws
	 */
	public static String castStringIntegerDate(Date date, Date defaultValue) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date dateValue = defaultValue;
		String dateString = "";
		if (date != null) {
			dateString = fmt.format(date);
		} else {
			dateString = fmt.format(dateValue);
		}

		return dateString;
	}
	
	/**
	 * 根据传入的格式，转换为不同的日期
	 * 
	 * @Title: castStringByDate  
	 * @author: xianjun
	 * @Description: TODO  
	 * @param date
	 * @param pattern
	 * @return
	 * @throws
	 */
	public static String castStringByDate(Date date, String pattern) {
		SimpleDateFormat fmt = new SimpleDateFormat(pattern);
		Date dateValue = new Date();
		String dateString = "";
		if (date != null) {
			dateString = fmt.format(date);
		} else {
			dateString = fmt.format(dateValue);
		}
		return dateString;
	}
	
	/**
	 * 转日期格式为字符串
	 */
	public static String castStringDate(Date date) {
		return castStringDate(date, new Date());
	}
	
	/**
	 * 转 字符串为日期格式
	 */
	public static Date castDateByString(String defaultDate){
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = fmt.parse(defaultDate);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
			return new Date();
		}
	}
	
	/**
	 * 转BigDecimal为字符串
	 */
	public static String castStringByDecimal(BigDecimal value, String pattern) {
		DecimalFormat fmt = new DecimalFormat(pattern);
		try {
			String tempValue = fmt.format(value.stripTrailingZeros());
			return tempValue;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "0";
		}
	}
	
	/**
	 * 转换字符串，判断是否非空
	 * @Title: castStringByString  
	 * @author: xianjun
	 * @Description: TODO  
	 * @param defaultString
	 * @return
	 * @throws
	 */
	public static String castStringByString(String defaultString) {
		String value = defaultString == null ? "" : defaultString;
		return value;
	}
	
	/**
	 * 去除html标签
	 * @Title: delHTMLTag  
	 * @author: xianjun
	 * @Description: TODO  
	 * @param htmlStr
	 * @return
	 * @throws
	 */
	public static String delHTMLTag(String htmlStr){ 
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式 
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式 
        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式 
         
        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
        Matcher m_script=p_script.matcher(htmlStr); 
        htmlStr=m_script.replaceAll(""); //过滤script标签 
         
        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE); 
        Matcher m_style=p_style.matcher(htmlStr); 
        htmlStr=m_style.replaceAll(""); //过滤style标签 
         
        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
        Matcher m_html=p_html.matcher(htmlStr); 
        htmlStr=m_html.replaceAll(""); //过滤html标签 

        return htmlStr.trim(); //返回文本字符串 
    }

	/**
	 * 通过身份证号码获取出生日期、性别、年龄
	 * @author xianjun
	 * @param certificateNo
	 * @return 返回的出生日期格式：1990-01-01   性别格式：F-女，M-男
	 */
	public static Map<String, String> getBirAgeSex(String certificateNo) {
		String birthday = "";
		String age = "";
		String sexCode = "";

		int year = Calendar.getInstance().get(Calendar.YEAR);
		char[] number = certificateNo.toCharArray();
		boolean flag = true;
		if (number.length == 15) {
			for (int x = 0; x < number.length; x++) {
				if (!flag) return new HashMap<String, String>();
				flag = Character.isDigit(number[x]);
			}
		} else if (number.length == 18) {
			for (int x = 0; x < number.length - 1; x++) {
				if (!flag) return new HashMap<String, String>();
				flag = Character.isDigit(number[x]);
			}
		}
		if (flag && certificateNo.length() == 15) {
			birthday = "19" + certificateNo.substring(6, 8) + "-"
					+ certificateNo.substring(8, 10) + "-"
					+ certificateNo.substring(10, 12);
			sexCode = Integer.parseInt(certificateNo.substring(certificateNo.length() - 3, certificateNo.length())) % 2 == 0 ? "F" : "M";
			age = (year - Integer.parseInt("19" + certificateNo.substring(6, 8))) + "";
		} else if (flag && certificateNo.length() == 18) {
			birthday = certificateNo.substring(6, 10) + "-"
					+ certificateNo.substring(10, 12) + "-"
					+ certificateNo.substring(12, 14);
			sexCode = Integer.parseInt(certificateNo.substring(certificateNo.length() - 4, certificateNo.length() - 1)) % 2 == 0 ? "F" : "M";
			age = (year - Integer.parseInt(certificateNo.substring(6, 10))) + "";
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("birthday", birthday);
		map.put("age", age);
		map.put("sexCode", sexCode);
		return map;
	}

	/**
	 * @Description 将Date类型的时间转为long类型的时间戳
	 * @Author SuXingYong
	 * @Date 2020/1/10 10:40
	 * @Param [date]
	 * @Return long
	 **/
	public static long castDateForLong(Date date){
		long result = 0;
		if (date != null){
			result = date.getTime();
		}
		return result;
	}

	/**
	 * @Description 将 yyyy-MM-dd HH:mm:ss 类型的时间字符串转换为long类型的时间戳
	 * @Author SuXingYong
	 * @Date 2020/1/10 10:46
	 * @Param [dateStr]
	 * @Return long
	 **/
	public static long castDateStrForLong(String dateStr){
		long result = 0;
		Date date = CastUtil.castDateByString(dateStr);
		if (date != null){
			result = date.getTime();
		}
		return result;
	}

	/**
	 * @Description 将java.sql.Timestamp转化为对应格式的String
	 * @Author SuXingYong
	 * @Date 2020/3/10 15:13
	 * @Param [time, strFormat]
	 * @Return java.lang.String
	 **/
	public static String castTimestampForString(java.sql.Timestamp time, String strFormat) {
		DateFormat df = new SimpleDateFormat(strFormat);
		String str = df.format(time);
		return str;
	}

	/**
	 * 获取某年某月的第一天
	 * @author xianjun
	 * @param year
	 * @param month
	 * @return
	 */
	public static String getFirstDayByMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		// 设置年份
		cal.set(Calendar.YEAR,year);
		// 设置月份
		cal.set(Calendar.MONTH, month-1);
		// 获取某月最小天数
		int firstDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
		// 设置最小天数
		cal.set(Calendar.DAY_OF_MONTH, firstDay);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		// 格式化
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String firstDayOfMonth = sdf.format(cal.getTime());
		return firstDayOfMonth;
	}

	/**
	 * 获取某年某月的最后一天
	 *
	 * @author xianjun
	 * @param year
	 * @param month
	 * @return
	 */
	public static String getLastDayByMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		// 设置年份
		cal.set(Calendar.YEAR, year);
		// 设置月份
		cal.set(Calendar.MONTH, month-1);
		// 获取某月最大天数
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		// 设置日历中月份的最大天数
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		// 格式化时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String lastDayOfMonth = sdf.format(cal.getTime());
		return lastDayOfMonth;
	}
}
