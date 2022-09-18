package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.FILE_SERVER_DIRECTORY;

import com.zs.library.my_health_pass_auth.pojo.FileDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.env.Environment;

@Slf4j
@RequiredArgsConstructor
class FileServerUtil {

  private final Environment environment;

  public void writeFileToServer(FileDocument document) {
    try {
      if (document.getBytes().length == 0 || document.getFilename() == null) {
        throw new RuntimeException();
      }

      val filePath = getFileServerDirectory(document.getFilename());
      Files.write(filePath, document.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public FileDocument readFileFromServer(String filename) {
    val filePath = getFileServerDirectory(filename);

    try (val inputStream = Files.newInputStream(filePath)) {
      return FileDocument.builder()
          .bytes(inputStream.readAllBytes())
          .filename(filename)
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteFileFromServer(String filename) {
    try {
      val filePath = getFileServerDirectory(filename);
      Files.delete(filePath);
    } catch (IOException e) {
      log.info("The following user profile picture: {} does not exit.", filename);
    }
  }

  private Path getFileServerDirectory(String filename) {
    val directory = environment.getRequiredProperty(FILE_SERVER_DIRECTORY);
    val directoryPath = Path.of(directory);

    return directoryPath.resolve(filename);
  }

}
