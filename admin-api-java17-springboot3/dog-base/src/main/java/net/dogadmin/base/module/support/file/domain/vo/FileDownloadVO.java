package net.dogadmin.base.module.support.file.domain.vo;

import lombok.Data;

@Data
public class FileDownloadVO {

    /**
     * 文件字节数据
     */
    private byte[] data;

    /**
     * 文件元数据
     */
    private FileMetadataVO metadata;


}
