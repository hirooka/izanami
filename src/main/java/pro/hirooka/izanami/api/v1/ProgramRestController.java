package pro.hirooka.izanami.api.v1;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.model.recorder.M4vTranscodingStatus;
import pro.hirooka.izanami.domain.model.recorder.RecordingStatus;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;
import pro.hirooka.izanami.domain.operator.IProgramOperator;
import pro.hirooka.izanami.domain.service.epg.IProgramService;
import pro.hirooka.izanami.domain.service.recorder.IRecorderService;

@Slf4j
@AllArgsConstructor
@RequestMapping("api/v1/programs")
@RestController
public class ProgramRestController {

  private final IProgramOperator programOperator;
  private final IProgramService programService;
  private final IRecorderService recorderService;

  @GetMapping(value = "now")
  List<Program> readNow() {
    return programService.readByNow(Instant.now().toEpochMilli());
  }

  @GetMapping()
  List<Program> read() {
    return programService.readOneDayByNow(Instant.now().toEpochMilli());
  }

  @GetMapping("{channelRecording}")
  List<Program> read(@PathVariable final int channelRecording) {
    return programService.read(channelRecording);
  }

  @GetMapping("{channelRecording}/{date}")
  List<Program> read(
      @PathVariable final int channelRecording,
      @PathVariable final String date
  ) {
    return programService.read(channelRecording, date);
  }

  @GetMapping("day")
  List<List<Program>> getOneDayFromNow() {
    return programOperator.getOneDayFromNow();
  }

  @PostMapping()
  ReservedProgram create(
      @Validated final ReservedProgram reservedProgram
  ) {

    log.info("reservation -> {}", reservedProgram.toString());
    final ReservedProgram createdReservedProgram = new ReservedProgram();
    createdReservedProgram.setChannel(reservedProgram.getChannel());
    createdReservedProgram.setTitle(reservedProgram.getTitle());
    createdReservedProgram.setDetail(reservedProgram.getDetail());
    createdReservedProgram.setStart(reservedProgram.getStart());
    createdReservedProgram.setBegin(reservedProgram.getStart());
    createdReservedProgram.setEnd(reservedProgram.getEnd());
    createdReservedProgram.setDuration(reservedProgram.getDuration());
    createdReservedProgram.setChannelRecording(reservedProgram.getChannelRecording());
    createdReservedProgram.setChannelRemoteControl(reservedProgram.getChannelRemoteControl());
    createdReservedProgram.setChannelName(reservedProgram.getChannelName());
    createdReservedProgram.setBeginDate(reservedProgram.getBeginDate());
    createdReservedProgram.setEndDate(reservedProgram.getEndDate());
    createdReservedProgram.setStartRecording(reservedProgram.getBegin());
    createdReservedProgram.setStopRecording(reservedProgram.getEnd());
    createdReservedProgram.setRecordingDuration(reservedProgram.getDuration());
    createdReservedProgram.setFileName("");
    createdReservedProgram.setRecordingStatus(RecordingStatus.Reserved);
    createdReservedProgram.setM4vTranscodingStatus(M4vTranscodingStatus.None);

    log.info("createdReservedProgram -> {}", createdReservedProgram.toString());
    return recorderService.create(createdReservedProgram);
  }
}
