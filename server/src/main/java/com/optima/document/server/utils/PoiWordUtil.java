package com.optima.document.server.utils;/**
 * Created by xianjun on 2019/3/22.16:30
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.Bookmarks;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文书替换工具类
 * @author xianjun
 * @version 2019/3/22 16:30  
 *
 */
public class PoiWordUtil {
    /**
     * word为doc后缀
     */
    private static final String DOC = "doc";
    /**
     * word为docx后缀
     */
    private static final String DOCX = "docx";
    /**
     * 服务器为windows
     */
    private static final String SYSTEM_TYPE_W = "Windows";
    /**
     * 服务器为Linux
     */
    private static final String SYSTEM_TYPE_L = "Linux";

    /**
     * 错误消息
     */
    private static String errorMsg = "";
    /**
     * 模板路径
     */
    private static String templatePath = "";
    /**
     * 新文件的路径
     */
    private static String newFilePath = "";

    /**
     * 文书临时存放的目录
     */
    private static String tempFilePath = "";

    /**
     * 替换模板方法
     * @param path  文书路径
     * @param tempPath 临时路径
     * @param paramMap  替换参数
     * @return
     * @throws Exception
     */
//    public static Map<String,String> executeWord(String path, String tempPath, Map<String, Object> paramMap) throws Exception {
//        //获取模板路径
//        getTemplate(path);
//        //获取新文件生成路径
//        getFileStore(path);
//        newFilePath = tempPath;
//        //判断模板类型
//        if(templatePath.endsWith(DOC)){
//            //处理03版word
//            //replaceWordDoc(paramMap);
//        }else if(templatePath.endsWith(DOCX)){
//            //处理07版word
//            replaceWordDocx(paramMap);
//        }
//
//        //返回处理结果(新生成文件路径、错误信息)
//        Map<String,String> map = new HashMap<String,String>();
//        map.put("newFilePath", newFilePath);
//        map.put("errorMsg", errorMsg);
//        return map;
//    }

    /**
     * 替换模板文本内容（2007）
     * @param paramMap  替换参数
     * @throws Exception
     */
    private static void replaceWordDocx(Map<String, Object> paramMap) throws Exception{
        InputStream is = new FileInputStream(tempFilePath);
        XWPFDocument doc = new XWPFDocument(is);
        Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
        Iterator<XWPFTable> it = doc.getTablesIterator();
        XWPFParagraph para;
        XWPFTable patab;
        //替换文本
        while (iterator.hasNext()) {
            para = iterator.next();
            replaceInParaDocx(doc, para, paramMap);
        }
        while (it.hasNext()) {
            patab = it.next();
            List<XWPFTableRow> rows = patab.getRows();
            for (int i = 0; i < rows.size(); i++) {
                XWPFTableRow row = rows.get(i);//读取每一列数据
                 List<XWPFTableCell> cells = row.getTableCells();
                for (int j = 0; j < cells.size(); j++) {
                    XWPFTableCell cell = cells.get(j);
                    //输出当前的单元格的数据
                    //System.out.print(cell.getText() + "\t");
                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                    for (XWPFParagraph xwpfParagraph : paragraphs) {
                        replaceInParaDocx(doc, xwpfParagraph, paramMap);
                    }
                }
                System.out.println();
            }
        }
        //写出文件
        OutputStream out = new FileOutputStream(newFilePath);
        doc.write(out);
        //关闭输出流
        close(out);
    }

    /**
     * 替换方法（2007）
     * @param para
     * @param paramMap
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static void replaceInParaDocx(XWPFDocument doc, XWPFParagraph para, Map<String, Object> paramMap) throws Exception {
        List<XWPFRun> runs;
        Matcher matcher;
        String imgPath = "";
        int width = 100,height = 100;
        String txt = para.getParagraphText();
        if (matcher(txt.trim()).find()) {
            runs = para.getRuns();
            for (int i=0; i<runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String runText = run.toString();
                matcher = matcher(runText);
                if (matcher.find()) {
                    Map<String,Object> infoMap = new HashMap<String,Object>();
                    if ((matcher = matcher(runText)).find()) {
                        System.err.println("查找替换标签：" + matcher.group());
                        String tx  = matcher.group().substring(2,matcher.group().length()-1);
                        if (paramMap.get(tx) instanceof Map) {
                            infoMap = (Map<String,Object>)paramMap.get(tx);
                            if (runText.contains("_img") || runText.contains("_sign") || runText.contains("_map")) {
                                imgPath = CastUtil.castString(infoMap.get("Text"));
                                width = CastUtil.castInt(infoMap.get("Width"));
                                height = CastUtil.castInt(infoMap.get("Height"));
                                if (imgPath !=null && !"".equalsIgnoreCase(imgPath)) {
                                    runText = "";
                                }
                            } else {
                                String tempText = CastUtil.castString(infoMap.get("Text"));
                                if (tempText != null && !"".equalsIgnoreCase(tempText)) {
                                    runText = CastUtil.castString(infoMap.get("Text"));
                                }
                            }
                        } else {
                            if (runText.contains("_img") || runText.contains("_sign") || runText.contains("_map")) {
                                imgPath = CastUtil.castString(paramMap.get(tx));
                                if (imgPath !=null && !"".equalsIgnoreCase(imgPath)) {
                                    runText = "";
                                }
                            } else {
                                String tempText = CastUtil.castString(paramMap.get(tx));
                                if (tempText != null && !"".equalsIgnoreCase(tempText)) {
                                    runText = tempText;
                                }
                            }
                        }
                    }
                    //直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun，把文本附加在当前文本后面，
                    //所以我们不能直接设值，需要先删除当前run,然后再自己手动插入一个新的run。
                    para.removeRun(i);
                    XWPFRun newRun = para.insertNewRun(i);
                    newRun.setText(runText);
                    if(!infoMap.isEmpty()){
                        //设置字体样式
                        setFont(newRun, infoMap);
                    }
                    if(StringUtils.isNoneEmpty(imgPath)){
                        //获得当前CTInline
                        CTInline inline = newRun.getCTR().addNewDrawing().addNewInline();
                        doc.addPictureData(new FileInputStream(imgPath), 5);
                        insertPicture(doc, inline, width, height);
                    }
                    imgPath = "";
                    //infoMap.clear();
                    System.err.println("替换");
                }
            }
        }
    }

    /**
     * 2007字体样式设置
     * @param run
     * @param param
     */
    private static void setFont(XWPFRun run, Map<String,Object> param) {
        CTRPr pRpr = null;
        if (run.getCTR() != null) {
            pRpr = run.getCTR().getRPr();
            if (pRpr == null) {
                pRpr = run.getCTR().addNewRPr();
            }
        }

        // 设置字体
        if(param.get("Name") != null){
            CTFonts fonts = pRpr.isSetRFonts()?pRpr.getRFonts():pRpr.addNewRFonts();
            fonts.setAscii(param.get("Name").toString());
            fonts.setEastAsia(param.get("Name").toString());
            fonts.setHAnsi(param.get("Name").toString());
        }
        //粗体
        run.setBold(param.get("Bold")!=null?true:false);
        //斜体
        run.setItalic(param.get("Italic")!=null?true:false);
        //下划线
        if (param.get("Underline") != null) {
            run.setUnderline(UnderlinePatterns.SINGLE);
        }
        //字体大小
        if(param.get("Size") != null){
            CTHpsMeasure sz = pRpr.isSetSz() ? pRpr.getSz() : pRpr.addNewSz();
            sz.setVal(new BigInteger(param.get("Size").toString()));
            CTHpsMeasure szCs = pRpr.isSetSzCs() ? pRpr.getSzCs() : pRpr.addNewSzCs();
            szCs.setVal(new BigInteger(param.get("Size").toString()));
        }
        //字体颜色
        if (param.get("Color") != null) {
            run.setColor(param.get("Color").toString());
        }
    }

    /**
     * 插入图片
     *
     * @param document
     * @param inline
     * @param width
     * @param height
     * @throws Exception
     */
    public static void insertPicture(XWPFDocument document, CTInline inline, int width, int height) throws Exception {
        int id = document.getAllPictures().size()-1;
        final int EMU = 9525;
        width *= EMU;
        height *= EMU;
        String blipId = document.getAllPictures().get(id).getPackageRelationship().getId();

        String picXml = ""
                + "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
                + "   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
                + "      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
                + "         <pic:nvPicPr>"
                + "            <pic:cNvPr id=\"" + id + "\" name=\"Generated\"/>"
                + "            <pic:cNvPicPr/>"
                + "         </pic:nvPicPr>"
                + "         <pic:blipFill>"
                + "            <a:blip r:embed=\"" + blipId + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>"
                + "            <a:stretch>"
                + "               <a:fillRect/>"
                + "            </a:stretch>"
                + "         </pic:blipFill>"
                + "         <pic:spPr>"
                + "            <a:xfrm>"
                + "               <a:off x=\"0\" y=\"0\"/>"
                + "               <a:ext cx=\"" + width + "\" cy=\"" + height + "\"/>"
                + "            </a:xfrm>"
                + "            <a:prstGeom prst=\"rect\">"
                + "               <a:avLst/>"
                + "            </a:prstGeom>"
                + "         </pic:spPr>"
                + "      </pic:pic>"
                + "   </a:graphicData>"
                + "</a:graphic>";

        inline.addNewGraphic().addNewGraphicData();
        XmlToken xmlToken = null;
        xmlToken = XmlToken.Factory.parse(picXml);
        inline.set(xmlToken);
        inline.setDistT(0);
        inline.setDistB(0);
        inline.setDistL(0);
        inline.setDistR(0);
        CTPositiveSize2D extent = inline.addNewExtent();
        extent.setCx(width);
        extent.setCy(height);
        CTNonVisualDrawingProps docPr = inline.addNewDocPr();
        docPr.setId(id);
        docPr.setName("IMG_" + id);
        docPr.setDescr("IMG_" + id);
    }

    /**
     * 替换模板文本内容（2003）
     * @param paramMap 替换参数
     * @throws Exception
     */
    private static void replaceWordDoc(Map<String, Object> paramMap) throws Exception{
        InputStream is = new FileInputStream(templatePath);
        HWPFDocument doc = new HWPFDocument(is);
        Range bodyRange = doc.getRange();
        //替换文本
        replaceInParaDoc(doc, bodyRange, paramMap);
        //输出新文件
        FileOutputStream out = new FileOutputStream(newFilePath);
        doc.write(out);
        out.flush();
        //关闭输出流
        out.close();
    }

    /**
     * 替换方法（2003）
     * @param range
     * @param paramMap
     */
    @SuppressWarnings("unchecked")
    private static void replaceInParaDoc(HWPFDocument doc, Range range, Map<String, Object> paramMap) {
        Matcher matcher;
        String imgPath = "";
        int width = 0,height = 0;
        while ((matcher = matcher(range.text())).find()) {
            String key = matcher.group();
            Map<String, Object> infoMap = (Map<String, Object>) paramMap.get(key);
            if (infoMap.get("Type") !=null && "img".equalsIgnoreCase(CastUtil.castString(infoMap.get("Type")))) {
                //处理图片
                imgPath = infoMap.get("Path").toString();
                width = Integer.parseInt(infoMap.get("Width").toString());
                height = Integer.parseInt(infoMap.get("Height").toString());
            } else {
                range.replaceText(matcher.group(), String.valueOf(infoMap.get("Text")));
            }
            if(StringUtils.isNoneEmpty(imgPath)){
                Bookmarks bookmarks = doc.getBookmarks();
                PicturesTable picturesTable = doc.getPicturesTable();
            }
            imgPath = "";
            /*int index = Integer.valueOf(matcher.group(1)) - 1;
            if(index >= params.size()) break;
            if(params.get(index) instanceof String){
                range.replaceText(matcher.group(), String.valueOf(params.get(index)));
            }else if(params.get(index) instanceof Map){
                Map<String,Object> infoMap = (Map<String,Object>)params.get(index);
                range.replaceText(matcher.group(), String.valueOf(infoMap.get("Text")));
            }*/
        }
    }

    /**
     * 正则匹配字符串
     * @param str 带替换字符串
     * @return
     */
    public static Matcher matcher(String str) {
        String remax = "\\$.+?}";
        //String remax = "\\$\\{(.+?)\\}"Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(remax);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    /**
     * 获取文件模板路径
     * @param  path 模板文件路径
     * @return
     * @throws Exception
     */
    private static void getTemplate(String path) throws Exception {
        //String path = UploadFileUtils.getPath("write");
        // String docPath = path + templateName + ".doc";
        //String docxPath = path + templateName + ".docx";
        templatePath = path;
    }

//    /**
//     * 获取文件存储路径
//     * @param  templatePath 模板文件名称
//     * @return
//     * @throws IOException
//     */
//    private static void getFileStore(String templatePath) throws IOException {
//        //操作系统类型(Windows、Linux)
//        String sysType = UploadFileUtils.getPath("fileserver.system.type");
//        //文件存储跟目录
//        String fileRoot = UploadFileUtils.getPath("fileserver.local.root");
//        //文件生产目录
//        String fileStore = UploadFileUtils.getPath("fileserver.store");
//        //生成文件名称
//        String name = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//
//        if(SYSTEM_TYPE_W.equals(sysType)){
//            //Windows
//            tempFilePath = fileRoot + fileStore + "/" ;
//        }else if(SYSTEM_TYPE_L.equals(sysType)){
//            //Linux
//            tempFilePath = File.separator + "usr/fileserver/";
//        }else{
//            //其他服务器系统处理
//        }
//
//        //生成目录不存在创建目录
//        File file = new File(tempFilePath);
//        if(!file.exists()){
//            file.mkdirs();
//        }
//        tempFilePath = tempFilePath + name + ".docx";
//        FileUtils.copyFile(templatePath, tempFilePath);
//    }


    /**
     * 关闭输出流
     * @param os
     */
    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭输入流
     * @param in
     */
    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * word转pdf
     * @throws Exception
     * @return
     */
//    public static byte[] wordToPdf(byte[] source) throws Exception{
//        // 临时文件
//        Path tempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);;
//        Files.write(tempFilePath, source);
//        String command = "D:/OpenOffice4/program/soffice.exe -headless -accept=\"socket,host=127.0.0.1,port=8100;urp;\"";
//        Process p = Runtime.getRuntime().exec(command);
//        OpenOfficeConnection connection = new SocketOpenOfficeConnection();
//        connection.connect();
//        // 转换
//        DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
//        converter.convert(new ByteArrayInputStream(source), tempFilePath.toString());
//        // 关闭连接
//        connection.disconnect();
//        // 关闭进程
//        p.destroy();
//        return null;
//    }

}
