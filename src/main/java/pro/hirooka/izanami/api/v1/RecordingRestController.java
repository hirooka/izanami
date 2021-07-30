package pro.hirooka.izanami.api.v1;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;
import pro.hirooka.izanami.domain.service.recorder.IRecorderService;

@Slf4j
@AllArgsConstructor
@RequestMapping("api/v1/recordings")
@RestController
public class RecordingRestController {

  private final IRecorderService recorderService;

  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  ResponseEntity<ReservedProgram> create(
      @RequestBody final ReservedProgram reservedProgram,
      final UriComponentsBuilder uriComponentsBuilder
  ) {
    final ReservedProgram createdReservedProgram = recorderService.create(reservedProgram);
    final URI uri = uriComponentsBuilder
        .path("/recorder/{id}")
        .buildAndExpand(createdReservedProgram.getId())
        .toUri();
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setLocation(uri);
    return new ResponseEntity<>(createdReservedProgram, httpHeaders, HttpStatus.CREATED);
  }

  @GetMapping("{id}")
  ReservedProgram read(@PathVariable final int id) {
    return recorderService.read(id);
  }

  @GetMapping()
  List<ReservedProgram> read() {
    return recorderService.read();
  }

  @PutMapping("{id}")
  ReservedProgram update(
      @PathVariable final int id, @RequestBody final ReservedProgram reservedProgram
  ) {
    reservedProgram.setId(id);
    return recorderService.update(reservedProgram);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable final int id) {
    recorderService.delete(id);
  }

  @DeleteMapping()
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete() {
    recorderService.deleteAll();
  }
}
