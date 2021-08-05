package com.hdw.web.base.system.controller;


import cn.hutool.core.date.DateUtil;
import com.hdw.common.core.api.CommonResult;
import com.luhuiguo.fastdfs.domain.StorePath;
import com.luhuiguo.fastdfs.exception.FdfsUnsupportStorePathException;
import com.luhuiguo.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author JacksonTu
 * @Descriptin 文件上传下载
 * @Date 2018年5月8日 上午10:12:45
 */
@Slf4j
public abstract class UpLoadController {

    /**
     * 文件上传路径前缀
     */
    @Value("${hdw.file-upload-prefix}")
    private String fileUploadPrefix;

    /**
     * 资源访问全路径前缀
     */
    @Value("${hdw.resource-access-url}")
    private String resourceAccessUrl;

    /**
     * FastDFS文件上传服务器名称
     */
    @Value("${fdfs.file-upload.server}")
    private String fdfsFileUploadServer;

    @Autowired
    private FastFileStorageClient fastFileStorageClient;


    /**
     * 单个文件上传
     *
     * @param file 前台传过来文件路径
     * @param dir  保存文件的相对路径,相对路径必须upload开头，例如upload/test
     * @return
     * @throws Exception
     */
    public Map<String, String> upload(MultipartFile file, String dir) {
        Map<String, String> params = new HashedMap();
        String resultPath = "";
        try {
            String savePath = "";
            if (StringUtils.isNotBlank(dir)) {
                savePath = fileUploadPrefix + File.separator + "upload" + File.separator
                        + dir + File.separator + DateUtil.format(new Date(), "yyyyMMdd") + File.separator;
            } else {
                savePath = fileUploadPrefix + File.separator + "upload" + File.separator
                        + DateUtil.format(new Date(), "yyyyMMdd") + File.separator;
            }
            // 保存文件
            String realFileName = file.getOriginalFilename();
            Long fileName = System.currentTimeMillis();
            File targetFile = new File(new File(savePath).getAbsolutePath() + File.separator + fileName + realFileName.substring(realFileName.indexOf(".")));
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            FileUtils.copyInputStreamToFile(file.getInputStream(), targetFile);// 复制临时文件到指定目录下
            if (StringUtils.isNotBlank(resourceAccessUrl)) {
                resultPath = resourceAccessUrl + "/upload/" + dir + "/"
                        + DateUtil.format(new Date(), "yyyyMMdd") + "/"
                        + fileName + realFileName.substring(realFileName.indexOf("."));
            } else {
                resultPath = "/upload/" + dir + "/"
                        + DateUtil.format(new Date(), "yyyyMMdd") + "/"
                        + fileName + realFileName.substring(realFileName.indexOf("."));
            }

            params.put("fileName", realFileName);
            params.put("filePath", resultPath);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return params;
    }

    /**
     * 多文件上传
     *
     * @param multipartFiles 文件
     * @param dir            保存文件的文件夹名称,,相对路径必须upload开头，例如upload/test
     * @return
     */
    public List<Map<String, String>> uploads(MultipartFile[] multipartFiles, String dir) {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            // 判断file数组不能为空并且长度大于0
            if (multipartFiles != null && multipartFiles.length > 0) {
                // 循环获取file数组中得文件
                for (MultipartFile file : multipartFiles) {
                    String savePath = "";
                    if (StringUtils.isNotBlank(dir)) {
                        savePath = fileUploadPrefix + File.separator + "upload" + File.separator
                                + dir + File.separator + DateUtil.format(new Date(), "yyyyMMdd") + File.separator;
                    } else {
                        savePath = fileUploadPrefix + File.separator + "upload" + File.separator
                                + DateUtil.format(new Date(), "yyyyMMdd") + File.separator;
                    }
                    // 保存文件
                    String realFileName = file.getOriginalFilename();
                    Long fileName = System.currentTimeMillis();
                    File targetFile = new File(new File(savePath).getAbsolutePath() + File.separator + fileName + realFileName.substring(realFileName.indexOf(".")));
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    FileUtils.copyInputStreamToFile(file.getInputStream(), targetFile);// 复制临时文件到指定目录下
                    if (StringUtils.isNotBlank(resourceAccessUrl)) {
                        String resultPath = resourceAccessUrl + "/upload/" + dir + "/"
                                + DateUtil.format(new Date(), "yyyyMMdd") + "/"
                                + fileName + realFileName.substring(realFileName.indexOf("."));
                        Map<String, String> params = new HashedMap();
                        params.put("fileName", realFileName);
                        params.put("filePath", resultPath);
                        list.add(params);
                    } else {
                        String resultPath = "/upload/" + dir + "/"
                                + DateUtil.format(new Date(), "yyyyMMdd") + "/"
                                + fileName + realFileName.substring(realFileName.indexOf("."));
                        Map<String, String> params = new HashedMap();
                        params.put("fileName", realFileName);
                        params.put("filePath", resultPath);
                        list.add(params);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return list;
    }

    /**
     * 从本地删除文件
     *
     * @param fileUrl http路径
     * @return
     */
    public Object deleteFileFromLocal(String fileUrl) {
        System.out.println("待删除文件路径：" + fileUrl);
        if (StringUtils.isBlank(fileUrl)) {
            return CommonResult.failed("文件删除失败");
        }
        String temp = fileUrl.substring(fileUrl.lastIndexOf("upload"));
        File file = new File(fileUploadPrefix + File.separator + temp);
        if (file.exists() && file.isFile()) {
            file.delete();
            return CommonResult.success("文件删除成功");
        } else {
            return CommonResult.failed("文件删除失败");
        }
    }

    /**
     * 上传文件到FastDFS
     *
     * @param localFilePath
     * @return
     * @throws RuntimeException
     */
    public Map<String, String> uploadToFastDFS(String localFilePath) {
        Map<String, String> params = new HashedMap();
        String path = "";
        try {
            byte[] bytes = getBytes(localFilePath);
            String fileName = "";
            if ((localFilePath != null) && (localFilePath.length() > 0)) {
                int dot = localFilePath.lastIndexOf(File.separator);
                if ((dot > -1) && (dot < (localFilePath.length() - 1))) {
                    fileName = localFilePath.substring(dot + 1, localFilePath.length());
                }
            }
            StorePath storePath = fastFileStorageClient.uploadFile(bytes, FilenameUtils.getExtension(fileName));
            System.out.println("上传文件路径：" + storePath.getFullPath());
            log.info("文件分组：" + storePath.getGroup() + "上传文件路径：" + storePath.getFullPath());
            path = fdfsFileUploadServer + "/" + storePath.getFullPath();
            params.put("fileName", fileName);
            params.put("filePath", path);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return params;
    }


    /**
     * 上传文件到FastDFS
     *
     * @param file
     * @return
     */
    public Map<String, String> uploadToFastDFS(File file) {
        Map<String, String> params = new HashedMap();
        String path = "";
        try {
            String fileName = file.getName();
            StorePath storePath =
                    fastFileStorageClient.uploadFile(IOUtils.toByteArray(new
                            FileInputStream(file)), FilenameUtils.getExtension(file.getName()));
            System.out.println("上传文件路径：" + storePath.getFullPath());
            log.info("文件分组：" + storePath.getGroup() + "上传文件路径：" + storePath.getFullPath());
            path = fdfsFileUploadServer + "/" + storePath.getFullPath();
            params.put("fileName", fileName);
            params.put("filePath", path);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return params;
    }


    /**
     * 上传文件到FastDFS
     *
     * @param file
     * @return
     */
    public Map<String, String> uploadToFastDFS(MultipartFile file) {
        Map<String, String> params = new HashedMap();
        String path = "";
        try {
            String fileName = file.getOriginalFilename();
            StorePath storePath = fastFileStorageClient.uploadFile(IOUtils.toByteArray(file.getInputStream()),
                    FilenameUtils.getExtension(file.getOriginalFilename()));
            System.out.println("上传文件路径：" + storePath.getFullPath());
            log.info("文件分组：" + storePath.getGroup() + "上传文件路径：" + storePath.getFullPath());
            path = fdfsFileUploadServer + "/" + storePath.getFullPath();
            params.put("fileName", fileName);
            params.put("filePath", path);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return params;
    }

    /**
     * 多文件上传到FastDFS
     *
     * @param fileList
     * @return
     */
    public List<Map<String, String>> uploadsToFastDFS(List<File> fileList) {
        List<Map<String, String>> list = new ArrayList<>();
        if (null != fileList && !fileList.isEmpty()) {
            for (File file : fileList) {
                try {
                    String fileName = file.getName();
                    StorePath storePath = fastFileStorageClient.uploadFile(IOUtils.toByteArray(new FileInputStream(file)), FilenameUtils.getExtension(file.getName()));
                    System.out.println("上传文件路径：" + storePath.getFullPath());
                    log.info("文件分组：" + storePath.getGroup() + "上传文件路径：" + storePath.getFullPath());
                    String path = fdfsFileUploadServer + "/" + storePath.getFullPath();
                    Map<String, String> params = new HashedMap();
                    params.put("fileName", fileName);
                    params.put("filePath", path);
                    list.add(params);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 多文件上传到FastDFS
     *
     * @param multipartFiles
     * @return
     */
    public List<Map<String, String>> uploadsToFastDFS(MultipartFile[] multipartFiles) {
        List<Map<String, String>> list = new ArrayList<>();
        if (multipartFiles != null && multipartFiles.length > 0) {
            for (MultipartFile file : multipartFiles) {
                try {
                    String fileName = file.getOriginalFilename();
                    StorePath storePath = fastFileStorageClient.uploadFile(IOUtils.toByteArray(file.getInputStream()), FilenameUtils.getExtension(file.getOriginalFilename()));
                    System.out.println("上传文件路径：" + storePath.getFullPath());
                    log.info("文件分组：" + storePath.getGroup() + "上传文件路径：" + storePath.getFullPath());
                    String path = fdfsFileUploadServer + "/" + storePath.getFullPath();
                    Map<String, String> params = new HashedMap();
                    params.put("fileName", fileName);
                    params.put("filePath", path);
                    list.add(params);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 从FastDFS上下载文件
     *
     * @param fileUrl  源文件路径
     * @param filePath 保存路径（不需要带文件名）
     * @return
     */
    public Object downloadFileFromFastDFS(String fileUrl, String filePath) {
        try {
            String temp = "";
            if (fileUrl.indexOf("?") > -1) {
                temp = fileUrl.substring(fileUrl.indexOf("group"), fileUrl.indexOf("?"));
            } else {
                temp = fileUrl.substring(fileUrl.indexOf("group"));
            }
            String group = temp.substring(0, temp.indexOf("/"));
            String path = temp.substring(temp.indexOf("/") + 1);
            String fileName = fileUrl.substring(fileUrl.indexOf("=") + 1);
            byte[] bfile = fastFileStorageClient.downloadFile(group, path);
            getFile(bfile, filePath, fileName);
            return CommonResult.success("文件下载成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 从FastDFS上下载文件
     *
     * @param fileUrl 源文件路径
     * @return
     */
    public byte[] downloadFileFromFastDFS(String fileUrl) {
        byte[] bfile = null;
        try {
            String temp = "";
            if (fileUrl.indexOf("?") > -1) {
                temp = fileUrl.substring(fileUrl.indexOf("group"), fileUrl.indexOf("?"));
            } else {
                temp = fileUrl.substring(fileUrl.indexOf("group"));
            }
            String group = temp.substring(0, temp.indexOf("/"));
            String path = temp.substring(temp.indexOf("/") + 1);
            bfile = fastFileStorageClient.downloadFile(group, path);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return bfile;
    }

    /**
     * 从FastDFS上删除文件
     *
     * @param fileUrl 源文件路径
     */
    public Object deleteFileFromFastDFS(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return CommonResult.failed("文件删除失败");
        }
        try {
            String temp = "";
            if (fileUrl.indexOf("?") > -1) {
                temp = fileUrl.substring(fileUrl.indexOf("group"), fileUrl.indexOf("?"));
            } else {
                temp = fileUrl.substring(fileUrl.indexOf("group"));
            }
            String group = temp.substring(0, temp.indexOf("/"));
            String path = temp.substring(temp.indexOf("/") + 1);
            fastFileStorageClient.deleteFile(group, path);
            return CommonResult.success("文件删除成功");
        } catch (FdfsUnsupportStorePathException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return CommonResult.failed(e.getMessage());
        }
    }

    /**
     * 获得指定文件的byte数组
     *
     * @param filePath
     * @return
     */
    private byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 根据byte数组，生成文件
     *
     * @param bfile
     * @param filePath
     * @param fileName
     */
    private void getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + "\\" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
