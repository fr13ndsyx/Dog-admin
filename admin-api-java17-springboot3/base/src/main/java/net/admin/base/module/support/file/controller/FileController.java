package net.admin.base.module.support.file.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.admin.base.common.constant.RequestHeaderConst;
import net.admin.base.common.controller.SupportBaseController;
import net.admin.base.common.domain.RequestUser;
import net.admin.base.common.domain.ResponseDTO;
import net.admin.base.common.util.RequestUtil;
import net.admin.base.common.util.ResponseUtil;
import net.admin.base.constant.SwaggerTagConst;
import net.admin.base.module.support.file.domain.vo.FileDownloadVO;
import net.admin.base.module.support.file.domain.vo.FileUploadVO;
import net.admin.base.module.support.file.service.FileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Tag(name = SwaggerTagConst.Support.FILE)
public class FileController extends SupportBaseController {

    @Resource
    private FileService fileService;


    @Operation(summary = "文件上传")
    @PostMapping("/file/upload")
    public ResponseDTO<FileUploadVO> upload(@RequestParam MultipartFile file, @RequestParam Integer folder) {
        RequestUser requestUser = RequestUtil.getRequestUser();
        return fileService.fileUpload(file, folder, requestUser);
    }

    @Operation(summary = "获取文件URL：根据fileKey")
    @GetMapping("/file/getFileUrl")
    public ResponseDTO<String> getUrl(@RequestParam String fileKey) {
        return fileService.getFileUrl(fileKey);
    }

    @Operation(summary = "下载文件流（根据fileKey）")
    @GetMapping("/file/downLoad")
    public void downLoad(@RequestParam String fileKey, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userAgent = JakartaServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT);
        ResponseDTO<FileDownloadVO> downloadFileResult = fileService.getDownloadFile(fileKey, userAgent);
        if (!downloadFileResult.getOk()) {
            ResponseUtil.write(response, downloadFileResult);
            return;
        }
        // 下载文件信息
        FileDownloadVO fileDownloadVO = downloadFileResult.getData();
        // 设置下载消息头
        ResponseUtil.setDownloadFileHeader(response, fileDownloadVO.getMetadata().getFileName(), fileDownloadVO.getMetadata().getFileSize());
        // 下载
        response.getOutputStream().write(fileDownloadVO.getData());
    }
}
