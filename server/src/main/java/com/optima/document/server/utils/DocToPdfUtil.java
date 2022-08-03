package com.optima.document.server.utils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author xianjun
 * @version 2017年4月20日 上午10:48:46 word转pdf工具类
 */
@Slf4j
public class DocToPdfUtil {

	private static final int wdFormatPDF = 17; // pdf格式
	private static final int xlTypePDF = 0;
	private static final int ppSaveAsPDF = 32;
	// 代表一个word 程序
	private static ActiveXComponent MsWordApp = null;
	// 代表进行处理的word 文档
	private static Dispatch document = null;
	// word文档
	private Dispatch doc;
	// word运行程序
	private ActiveXComponent word;
	// 所有word文档集合
	private Dispatch documents;
	// 选定的范围和插入点
	private Dispatch selection;

	private boolean saveOnExit = true;

	public DocToPdfUtil() throws Exception {
		ComThread.InitSTA();
		if (word == null) {
			word = new ActiveXComponent("Word.Application");
			// 不可见打开word
			word.setProperty("Visible", new Variant(false));
			// 禁用宏
			word.setProperty("AutomationSecurity", new Variant(3));
		}

		if (documents == null)
			documents = word.getProperty("Documents").toDispatch();
	}

	/**
	 * 设置退出时参数
	 *
	 * @Title: setSaveOnExit
	 * @author: xianjun
	 * @Description: TODO
	 * @param saveOnExit true-退出时保存文件，false-退出时不保存文件
	 * @throws
	 */
	public void setSaveOnExit(boolean saveOnExit) {
		this.saveOnExit = saveOnExit;
	}

	/**
	 * 打开一个已存在的文档
	 *
	 * @Title: openDocument
	 * @author: xianjun
	 * @Description: TODO
	 * @param docPath
	 * @throws
	 */
	public void openDocument(String docPath) {
		closeDocument();
		doc = Dispatch.call(documents, "Open", docPath).toDispatch();
		selection = Dispatch.get(word, "Selection").toDispatch();
	}

	/**
	 * 文件保存或另存为路径
	 *
	 * @Title: save
	 * @author: xianjun
	 * @Description: TODO
	 * @param savePath
	 * @throws
	 */
	public void save(String savePath) {
		Dispatch.call(Dispatch.call(word, "WordBasic").getDispatch(), "FileSaveAs", savePath);
	}

	/**
	 * 关闭文档
	 * @Title: closeDocument
	 * @author: xianjun
	 * @Description: TODO
	 * @param val 0不保存修改 -1 保存修改 -2 提示是否保存修改
	 * @throws
	 */
	public void closeDocument(int val) {
		Dispatch.call(doc, "Close", new Variant(val));
		doc = null;
	}

	/**
	 * 关闭当前word文档
	 *
	 * @Title: cloaseDocument
	 * @author: xianjun
	 * @Description: TODO
	 * @throws
	 */
	public void cloaseDocument() {
		if (doc != null) {
			Dispatch.call(doc, "Save");
			Dispatch.call(doc, "Close", new Variant(saveOnExit));
			doc = null;
		}
	}

	/**
	 * 关闭全部应用
	 *
	 * @Title: close
	 * @author: xianjun
	 * @Description: TODO
	 * @throws
	 */
	public void close() {
		if (word != null) {
			//Dispatch.call(word, "Quit");
			word.invoke("Quit",0);
			word = null;
		}
		selection = null;
		documents = null;
		ComThread.Release();
	}

	/**
	 * word转pdf
	 *
	 * @author xianjun
	 * @version 2017年4月20日 上午9:56:09
	 * @describe
	 * @param fromAddress
	 *            待转地址
	 * toAddress 新文件地址
	 */
//	public static String wordToPdf(String fromAddress) {
//		//ActiveXComponent ax = null;
//		String toAddress = splitSuffx(fromAddress);
//		try {
//			PoiWordUtil.wordToPdf(fromAddress,toAddress);
//			return toAddress;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		/*try {
//			long startTime = System.currentTimeMillis();
//
//			*//**
//		 * 创建不同的控件调用不同的值 Word-Word.Application Excel-Excel.Application
//		 * Powerpoint-Powerpoint.Application Outlook-Outlook.Application
//		 *//*
//			ax = new ActiveXComponent("Word.Application");
//			*//**
//		 * 设置打开文件不可见
//		 *//*
//			ax.setProperty("Visible", false);
//			*//**
//		 * 获取word文档中所有内容
//		 *//*
//			Dispatch docs = ax.getProperty("Documents").toDispatch();
//			*//**
//		 * 打开word文档，并设置word为不可编辑和不需确认
//		 *//*
//			Dispatch doc = Dispatch.call(docs, "Open", fromAddress, false, true).toDispatch();
//			File tofile = new File(toAddress);
//			if (tofile.exists()) {
//				tofile.delete();
//			}
//			// word文件另存为pdf文件
//			Dispatch.call(doc, "SaveAs", toAddress, wdFormatPDF);
//			// 关闭word文档
//			Dispatch.call(doc, "Close", false);
//			long endTime = System.currentTimeMillis();
//			System.out.println("转换完成，总共耗时" + (endTime - startTime));
//			return toAddress;
//		} catch (Exception e) {
//			System.out.println("=============Error:文档转换失败：" + e.getMessage());
//			return null;
//		} */
//	}

//	public static String wordToPdf(String fromAddress, String fileName) throws Exception{
//		String fileRootPath = UploadFileUtils.getPath("write");
//		String dateUrl = CastUtil.castStringByDate(new Date(), "yyyy-MM-dd");
//		String htmlFileDirPath =  File.separator + dateUrl;
//		File htmlDirFile = new File(fileRootPath + htmlFileDirPath);
//		if (!htmlDirFile.exists()) {
//			htmlDirFile.mkdirs();
//		}
//		//html文件路径
//		String pdfFilePath = htmlDirFile.getPath() + File.separator + fileName + "." + "pdf";
//		long startTime = System.currentTimeMillis();
//		File tofile = new File(pdfFilePath);
//		if (tofile.exists()) {
//			tofile.delete();
//		}
//		// word文件另存为pdf文件
//		PoiWordUtil.wordToPdf(fromAddress,pdfFilePath);
//		// 关闭word文档
//		long endTime = System.currentTimeMillis();
//		System.out.println("转换完成，总共耗时" + (endTime - startTime));
//		return "/"+dateUrl + "/" + fileName + "." + "pdf";
//	}

	public static String excel2PDF(String fromAddress){
		String toAddress = splitSuffx(fromAddress);
		try{
			long startTime = System.currentTimeMillis();
			ActiveXComponent app = new ActiveXComponent("Excel.Application");
			app.setProperty("Visible", false);
			Dispatch excels = app.getProperty("Workbooks").toDispatch();
			Dispatch excel = Dispatch.call(excels,"Open",fromAddress,false,true).toDispatch();
			File tofile = new File(toAddress);
			if (tofile.exists()) {
				tofile.delete();
			}
			Dispatch.call(excel,"ExportAsFixedFormat",xlTypePDF,toAddress);
			Dispatch.call(excel, "Close",false);
			long endTime = System.currentTimeMillis();
			System.out.println("转换完成，总共耗时" + (endTime - startTime));
			return toAddress;
		} catch (Exception e) {
			System.out.println("=============Error:文档转换失败：" + e.getMessage());
			return null;
		}

	}
	public static String ppt2PDF(String fromAddress){
		String toAddress = splitSuffx(fromAddress);
		try{
			long startTime = System.currentTimeMillis();
			ActiveXComponent app = new ActiveXComponent("PowerPoint.Application");
			//app.setProperty("Visible", msofalse);
			Dispatch ppts = app.getProperty("Presentations").toDispatch();

			Dispatch ppt = Dispatch.call(ppts,
					"Open",
					fromAddress,
					true,//ReadOnly
					true,//Untitled指定文件是否有标题
					false//WithWindow指定文件是否可见
			).toDispatch();

			Dispatch.call(ppt,
					"SaveAs",
					toAddress,
					ppSaveAsPDF
			);

			Dispatch.call(ppt, "Close");
			long endTime = System.currentTimeMillis();
			System.out.println("转换完成，总共耗时" + (endTime - startTime));
			return toAddress;
		} catch (Exception e) {
			System.out.println("=============Error:文档转换失败：" + e.getMessage());
			return null;
		}
	}

	/**
	 * 路径处理函数，提取路径后缀
	 *
	 * @author xianjun
	 * @version 2017年4月20日 上午10:54:49
	 * @describe
	 * @param fromAddress
	 * @return
	 */
	public static String splitSuffx(String fromAddress) {
		String toAddress = fromAddress.substring(0, fromAddress.lastIndexOf(".") + 1);
		toAddress = toAddress + "pdf";
		return toAddress;
	}

	/**
	 * 从选定内容或插入点开始查找文本
	 *
	 * @param toFindText
	 *            要查找的文本
	 * @return boolean true-查找到并选中该文本，false-未查找到文本
	 */
	public static boolean find(ActiveXComponent ax,String toFindText) {
		if (toFindText == null || toFindText.equals(""))
			return false;
		Dispatch selection = Dispatch.get(ax, "Selection").toDispatch(); // 输入内容需要的对象
		// 从selection所在位置开始查询
		Dispatch find1 = Dispatch.call(selection, "Find").toDispatch();
		// 设置要查找的内容
		Dispatch.put(find1, "Text", toFindText);
		// 向前查找
		Dispatch.put(find1, "Forward", "True");
		// 设置格式
		Dispatch.put(find1, "Format", "True");
		// 大小写匹配
		Dispatch.put(find1, "MatchCase", "True");
		// 全字匹配
		Dispatch.put(find1, "MatchWholeWord", "True");
		// 查找并选中
		return Dispatch.call(find1, "Execute").getBoolean();
	}

	// 向文档中添加 一个图片，
	public static void insertJpeg(ActiveXComponent ax,String toFindText,String jpegFilePath,int width,int height) {
		Dispatch selection = Dispatch.get(ax, "Selection").toDispatch();
//        Dispatch image = Dispatch.get(selection, "InLineShapes").toDispatch();
//        Dispatch.call(image, "AddPicture", jpegFilePath);


		if (find(ax,toFindText)) {
			Dispatch picture = Dispatch.call(Dispatch.get(selection, "InLineShapes").toDispatch(), "AddPicture", jpegFilePath).toDispatch(); // 添加图片
			Dispatch.call(picture, "Select"); // 选中图片
			Dispatch.put(picture, "Width", new Variant(width)); // 图片的宽度
			Dispatch.put(picture, "Height", new Variant(height)); // 图片的高度
			Dispatch ShapeRange = Dispatch.call(picture, "ConvertToShape").toDispatch(); // 取得图片区域
			Dispatch WrapFormat = Dispatch.get(ShapeRange, "WrapFormat").toDispatch(); // 取得图片的格式对象
			Dispatch.put(WrapFormat, "Type", 7); // 设置环绕格式（0 - 7）下面是参数说明

			//	wdWrapInline 7 将形状嵌入到文字中。
			//	wdWrapNone 3 将形状放在文字前面。请参阅 wdWrapFront 。
			//	wdWrapSquare 0 使文字环绕形状。行在形状的另一侧延续。
			//	wdWrapThrough 2 使文字环绕形状。
			//	wdWrapTight 1 使文字紧密地环绕形状。
			//	wdWrapTopBottom 4 将文字放在形状的上方和下方。
			//	wdWrapBehind 5 将形状放在文字后面。
			//	wdWrapFront 6 将形状放在文字前面。
		}
	}
	// 保存文档的更改
	public static void save() {
		Dispatch.call(document, "Save");
	}
	public static void closeDocument() {
		// Close the document without saving changes
		// 0 = wdDoNotSaveChanges
		// -1 = wdSaveChanges
		// -2 = wdPromptToSaveChanges
		if(document != null) {
			Dispatch.call(document, "Close", new Variant(false));
			document = null;
		}
	}
	public static void closeWord() {
		Dispatch.call(MsWordApp, "Quit");
		MsWordApp = null;
		document = null;
	}

	public static void openAnExistsFileTest(String wordFilePath,String toFindText,String imagePath,int width,int height) {
		ActiveXComponent ax = null;
		ax = new ActiveXComponent("Word.Application");
		ax.setProperty("Visible", new Variant(false));// 是否前台打开word 程序，或者后台运行
		Dispatch documents = Dispatch.get(ax, "Documents").toDispatch();
		documents = Dispatch.call(documents, "Open", wordFilePath,
				new Variant(true)/* 是否进行转换ConfirmConversions */,
				new Variant(false)/* 是否只读 */).toDispatch();
		//Dispatch selection = Dispatch.get(MsWordApp, "Selection").toDispatch();
		insertJpeg(ax,toFindText,imagePath,width,height); // 插入图片(注意刚打开的word，光标处于开头，故，图片在最前方插入)
		// word文件另存为pdf文件
		String toAddress = splitSuffx(wordFilePath);
		Dispatch.call(documents, "Save");
		//Dispatch.call(documents, "Quit");
		Dispatch.call(documents, "SaveAs", toAddress, wdFormatPDF);
		Dispatch.call(documents, "Close", new Variant(false));
		//save();
		//closeDocument();
		//closeWord();
		// 关闭word文档
		//Dispatch.call(documents, "Close", false);
	}

	public boolean findText(Dispatch selection, String toFindText,String matchWholeWord) {
		return findText(selection, toFindText, matchWholeWord, false);
	}

	/**
	 * 查找文本
	 * @param selection
	 * @param toFindText
	 * @param matchWholeWord 匹配全词
	 * @param matchWildcards 使用通配符
	 * @return
	 */
	public boolean findText(Dispatch selection, String toFindText,String matchWholeWord, boolean matchWildcards) {
		if (toFindText == null || "".equals(toFindText))
			return false;
		// 从selection所在位置开始查询
		Dispatch find1 = Dispatch.call(selection, "Find").toDispatch();
		// 设置要查找的内容
		Dispatch.put(find1, "Text", toFindText);
		// 向前查找
		Dispatch.put(find1, "Forward", "True");
		// 通配符
		Dispatch.put(find1, "MatchWildcards", matchWildcards);
		// 设置格式
		Dispatch.put(find1, "Format", "True");
		// 大小写匹配
		Dispatch.put(find1, "MatchCase", "False");
		// 全字匹配
		Dispatch.put(find1, "MatchWholeWord", matchWholeWord);
		// 查找并选中
		return Dispatch.call(find1, "Execute").getBoolean();
	}

	/**
	 * 插入图片
	 *
	 * @Title: insertJpeg
	 * @author: xianjun
	 * @Description: TODO
	 * @param source
	 * @param toFindText
	 * @param imgSource
	 * @param width
	 * @param height
	 * @throws
	 */
	public static byte[] insertJpeg(byte[] source, String toFindText, byte[] imgSource, int width, int height) {
		long t1 = System.currentTimeMillis();
		DocToPdfUtil docToPdfUtil = null;
		Path tempFilePath = null;
		Path imgTempFilePath = null;
		byte[] result = null;
		try {
			docToPdfUtil = new DocToPdfUtil();
			tempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);
			Files.write(tempFilePath, source);
			docToPdfUtil.openDocument(tempFilePath.toString());
			if (docToPdfUtil.findText(docToPdfUtil.selection, toFindText,"True")) {
				imgTempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);
				Files.write(imgTempFilePath, imgSource);
				Dispatch picture = Dispatch.call(Dispatch.get(docToPdfUtil.selection, "InLineShapes").toDispatch(), "AddPicture", imgTempFilePath.toString()).toDispatch(); // 添加图片
				Dispatch.call(picture, "Select"); // 选中图片
				if (width!=0) {
					Dispatch.put(picture, "Width", new Variant(width)); // 图片的宽度
					Dispatch.put(picture, "Height", new Variant(height)); // 图片的高度
				}

//				Dispatch ShapeRange = Dispatch.call(picture, "ConvertToShape").toDispatch(); // 取得图片区域
//				Dispatch WrapFormat = Dispatch.get(ShapeRange, "WrapFormat").toDispatch(); // 取得图片的格式对象
//				Dispatch.put(WrapFormat, "Type", 0); // 设置环绕格式（0 - 7）下面是参数说明
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
			if (tempFilePath != null) {
				try {
					result = Files.readAllBytes(tempFilePath);
					Files.delete(tempFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (imgTempFilePath != null) {
				try {
					Files.delete(imgTempFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.info("insert image=======consuming：{} milliseconds", System.currentTimeMillis() - t1);
		return result;
	}

	/**
	 * 插入图片
	 *
	 * @Title: insertJpeg
	 * @author: xianjun
	 * @Description: TODO
	 * @param source
	 * @param toFindText
	 * @param imgSources
	 * @param width
	 * @param height
	 * @throws
	 */
	public static byte[] insertJpeg(byte[] source, String toFindText, List<byte[]> imgSources, int width, int height) {
		DocToPdfUtil docToPdfUtil = null;
		Path tempFilePath = null;
		Path imgTempFilePath = null;
		byte[] result = null;
		try {
			docToPdfUtil = new DocToPdfUtil();
			tempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);
			Files.write(tempFilePath, source);
			docToPdfUtil.openDocument(tempFilePath.toString());
			String replaceText = "";
			for (int i = 0; i < imgSources.size(); i++) {
				replaceText += "${" + toFindText + "_" + i + "}";
			}
			String searchText = "${" + toFindText + "}";
			boolean flag = docToPdfUtil.findText(docToPdfUtil.selection, searchText, "True");
			if (flag) {
				setKeyAndValue(docToPdfUtil, replaceText, searchText);
			}
			for (int i = 0; i < imgSources.size(); i++) {
				Dispatch.call(docToPdfUtil.selection,"HomeKey",new Variant(6));
				if (docToPdfUtil.findText(docToPdfUtil.selection, "${" + toFindText + "_" + i + "}" ,"True")) {
					imgTempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);
					Files.write(imgTempFilePath, imgSources.get(i));
					Dispatch picture = Dispatch.call(Dispatch.get(docToPdfUtil.selection, "InLineShapes").toDispatch(), "AddPicture", imgTempFilePath.toString()).toDispatch(); // 添加图片
					Dispatch.call(picture, "Select"); // 选中图片
					if (width != 0) {
						Dispatch.put(picture, "Width", new Variant(width)); // 图片的宽度
						Dispatch.put(picture, "Height", new Variant(height)); // 图片的高度
					}
//				Dispatch ShapeRange = Dispatch.call(picture, "ConvertToShape").toDispatch(); // 取得图片区域
//				Dispatch WrapFormat = Dispatch.get(ShapeRange, "WrapFormat").toDispatch(); // 取得图片的格式对象
//				Dispatch.put(WrapFormat, "Type", 0); // 设置环绕格式（0 - 7）下面是参数说明
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
			if (tempFilePath != null) {
				try {
					result = Files.readAllBytes(tempFilePath);
					Files.delete(tempFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (imgTempFilePath != null) {
				try {
					Files.delete(imgTempFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static void insertJpeg(String wordFilePath, List<String> findTexts, List<String> imagePaths, List<Map<String, Object>> imageSelections) {
		DocToPdfUtil docToPdfUtil = null;
		try {
			docToPdfUtil = new DocToPdfUtil();
			docToPdfUtil.openDocument(wordFilePath);
			if (findTexts != null && findTexts.size() > 0) {
				for (int i=0; i< findTexts.size(); i++) {
					String toFindText = findTexts.get(i);
					String imagePath = imagePaths.get(i);
					Map<String, Object> imageSelection = imageSelections.get(i);
					int height = CastUtil.castInt(imageSelection.get("Height"));
					int width = CastUtil.castInt(imageSelection.get("Width"));
					if (docToPdfUtil.findText(docToPdfUtil.selection, toFindText,"True")) {
						Dispatch picture = Dispatch.call(Dispatch.get(docToPdfUtil.selection, "InLineShapes").toDispatch(), "AddPicture", imagePath).toDispatch(); // 添加图片
						Dispatch.call(picture, "Select"); // 选中图片
						if (width!=0) {
							Dispatch.put(picture, "Width", new Variant(width)); // 图片的宽度
							Dispatch.put(picture, "Height", new Variant(height)); // 图片的高度
						}

						Dispatch ShapeRange = Dispatch.call(picture, "ConvertToShape").toDispatch(); // 取得图片区域
						Dispatch WrapFormat = Dispatch.get(ShapeRange, "WrapFormat").toDispatch(); // 取得图片的格式对象
						Dispatch.put(WrapFormat, "Type", 0); // 设置环绕格式（0 - 7）下面是参数说明
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
		}
	}

	/**
	 * 替换字段
	 *
	 * @Title: wordFindReplace
	 * @author: xianjun
	 * @Description: TODO
	 * @param wordpath
	 * @param oldtext
	 * @param newtext
	 * @return
	 * @throws
	 */
	public static String wordFindReplace(String wordpath,String oldtext,String newtext) {
		DocToPdfUtil docToPdfUtil = null;
		try {
			docToPdfUtil = new DocToPdfUtil();
			docToPdfUtil.openDocument(wordpath);
			boolean flag = docToPdfUtil.findText(docToPdfUtil.selection, oldtext,"True");
			if (flag) {
				Dispatch.put(docToPdfUtil.selection,"Text",newtext);
				Dispatch.call(docToPdfUtil.selection, "MoveRight");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
		}
		return null;
	}

	/**
	 * 向word填充属性字段
	 *
	 * @Title: fieldToWord
	 * @author: xianjun
	 * @Description: TODO
	 * @param source 源
	 * @param infoMap 属性字段
	 * @return
	 * @throws
	 */
	public static byte[] fieldToWord(byte[] source, Map<String, Object> infoMap) {
		long t1 = System.currentTimeMillis();
		DocToPdfUtil docToPdfUtil = null;
		Path tempFilePath = null;
		byte[] result = null;
		try {
			docToPdfUtil = new DocToPdfUtil();
			tempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);
			Files.write(tempFilePath, source);
			docToPdfUtil.openDocument(tempFilePath.toString());
			for (String key : infoMap.keySet()) {
				String replaceText = "${" + key.trim() + "}";
				boolean flag = docToPdfUtil.findText(docToPdfUtil.selection, replaceText,"True");
				if (flag) {
					String value = CastUtil.castString(infoMap.get(key));
					setKeyAndValue(docToPdfUtil, value, replaceText);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
			if (tempFilePath != null) {
				try {
					result = Files.readAllBytes(tempFilePath);
					Files.delete(tempFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.info("insert text=======consuming：{} milliseconds", System.currentTimeMillis() - t1);
		return result;
	}

	/**
	 * 清除占位符
	 * @param source
	 * @return
	 */
	public static byte[] clearPlaceholder(byte[] source) {
		long t1 = System.currentTimeMillis();
		DocToPdfUtil docToPdfUtil = null;
		Path tempFilePath = null;
		byte[] result = null;
		try {
			docToPdfUtil = new DocToPdfUtil();
			tempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);
			Files.write(tempFilePath, source);
			docToPdfUtil.openDocument(tempFilePath.toString());
			String replaceText = "$\\{*\\}";
			boolean flag = docToPdfUtil.findText(docToPdfUtil.selection, replaceText, "True", true);
			if (flag) {
				setKeyAndValue(docToPdfUtil, "", replaceText, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
			if (tempFilePath != null) {
				try {
					result = Files.readAllBytes(tempFilePath);
					Files.delete(tempFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.info("clear placeholder=======consuming：{} milliseconds", System.currentTimeMillis() - t1);
		return result;
	}

	public static void setKeyAndValue(DocToPdfUtil docToPdfUtil, String value, String replaceText) {
		setKeyAndValue(docToPdfUtil, value, replaceText, false);
	}

	public static void setKeyAndValue(DocToPdfUtil docToPdfUtil, String value,String replaceText, boolean matchWildcards) {
		if (value == null) {
			value = "";
		}
		value = CastUtil.delHTMLTag(value);
		if (value.indexOf("\n\n") != -1) {
			value = value.replace("\n\n", CastUtil.castString((char) 11));
		}
		if (value.indexOf("\n") != -1) {
			value = value.replace("\n", CastUtil.castString((char) 11));
		}

		Dispatch.put(docToPdfUtil.selection, "Text", value);

		//Dispatch.call(docToPdfUtil.selection, "MoveStart");
		Dispatch.call(docToPdfUtil.selection,"HomeKey",new Variant(6));
		boolean flag = docToPdfUtil.findText(docToPdfUtil.selection, replaceText,"True", matchWildcards);
		if (flag) {
			setKeyAndValue(docToPdfUtil, value, replaceText, matchWildcards);
		}
	}

	/**
	 * word转pdf
	 * @Title: wordCnoverPdf
	 * @author: xianjun
	 * @Description: TODO
	 * @param fromAddress
	 * @return
	 * @throws
	 */
	public static String wordCnoverPdf(String fromAddress) {
		String toAddress = splitSuffx(fromAddress);
		DocToPdfUtil docToPdfUtil = null;
		try {
			docToPdfUtil = new DocToPdfUtil();
			long startTime = System.currentTimeMillis();
			docToPdfUtil = new DocToPdfUtil();
			docToPdfUtil.openDocument(fromAddress);
			File tofile = new File(toAddress);
			if (tofile.exists()) {
				tofile.delete();
			}
			// word文件另存为pdf文件
			Dispatch.call(docToPdfUtil.doc, "SaveAs", toAddress, wdFormatPDF);
			// 关闭word文档
			long endTime = System.currentTimeMillis();
			System.out.println("转换完成，总共耗时" + (endTime - startTime));
			if(docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
			return toAddress;
		} catch (Exception e) {
			if(docToPdfUtil != null) {
				docToPdfUtil.cloaseDocument();
				docToPdfUtil.close();
			}
			System.out.println("=============Error:文档转换失败：" + e.getMessage());
			return null;
		}

	}

}