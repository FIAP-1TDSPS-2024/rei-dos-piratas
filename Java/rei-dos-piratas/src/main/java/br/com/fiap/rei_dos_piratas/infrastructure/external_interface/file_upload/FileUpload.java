package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.file_upload;

import java.net.URL;
import java.time.Duration;

public interface FileUpload {
    URL gerarUrlPreAssinadaUpload(String destino, String fileName, Duration duration);
}
