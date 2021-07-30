package pro.hirooka.izanami.app;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.hirooka.izanami.domain.activity.IRecorderActivity;
import pro.hirooka.izanami.domain.model.epg.Program;
import pro.hirooka.izanami.domain.model.recorder.M4vTranscodingStatus;
import pro.hirooka.izanami.domain.model.recorder.RecordingStatus;
import pro.hirooka.izanami.domain.model.recorder.ReservedProgram;
import pro.hirooka.izanami.domain.operator.IProgramOperator;
import pro.hirooka.izanami.domain.service.epg.IProgramService;

@Slf4j
@AllArgsConstructor
@RequestMapping("programs")
@Controller
public class ProgramController {

  private final IProgramService programService;
  private final HttpServletRequest httpServletRequest;
  private final IRecorderActivity recorderActivity;
  private final IProgramOperator programOperator;

  @GetMapping(value = "now")
  String readNow(final Model model) {
    model.addAttribute(
        "programList",
        programService.readByNow(Instant.now().toEpochMilli())
    );
    return "programs/list";
  }

  @GetMapping()
  String read(final Model model) {
    final List<Program> programList = programService.readOneDayByNow(Instant.now().toEpochMilli());
    model.addAttribute("programList", programList);
    return "programs/list";
  }

  @GetMapping("{channelRemoteControl}")
  String read(@PathVariable final int channelRemoteControl, final Model model) {
    model.addAttribute("programList", programService.read(channelRemoteControl));
    return "programs/list";
  }

  @GetMapping("{channelRemoteControl}/{date}")
  String read(
      @PathVariable final int channelRemoteControl,
      @PathVariable final String date,
      final Model model
  ) {
    final List<Program> programList = programService.read(channelRemoteControl, date);
    model.addAttribute("programList", programList);
    return "programs/list";
  }

  @GetMapping("{channelRemoteControl}/day")
  String getOneDayFromNowByChannelRemoteControl(
      @PathVariable final int channelRemoteControl,
      final Model model
  ) {
    model.addAttribute(
        "programList",
        programService.getOneDayFromNowByChannelRemoteControl(channelRemoteControl)
    );
    return "programs/list";
  }

  @GetMapping("day")
  String getOneDayFromNow(final Model model) {
    final List<Integer> hourList = getHourList();
    model.addAttribute("hourList", hourList);
    final List<List<Program>> listOfProgramList = programOperator.getOneDayFromNow();
    listOfProgramList.forEach(programList -> {
      programList.forEach(program -> {

        final Instant instant = Instant.ofEpochMilli(new Date().getTime());
        final ZonedDateTime beginZonedDateTime =
            ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        final ZonedDateTime thresholdZonedDateTime = ZonedDateTime.of(
            beginZonedDateTime.getYear(),
            beginZonedDateTime.getMonthValue(),
            beginZonedDateTime.getDayOfMonth(),
            beginZonedDateTime.getHour(),
            0,
            0,
            0,
            ZoneId.systemDefault()
        );
        final ZonedDateTime endZonedDateTime =
            ZonedDateTime.from(instant.atZone(ZoneId.systemDefault())).plusDays(1);
        final ZonedDateTime e = ZonedDateTime.of(
            endZonedDateTime.getYear(),
            endZonedDateTime.getMonthValue(),
            endZonedDateTime.getDayOfMonth(),
            endZonedDateTime.getHour(),
            0,
            0,
            0,
            ZoneId.systemDefault()
        );

        final long now = thresholdZonedDateTime.toEpochSecond() * 1000;
        final long tomorrow = e.toEpochSecond() * 1000;
        if (now >= program.getBegin()) {
          long duration = program.getDuration() - (now - program.getBegin());
          int height = (2 * (int) duration / 60);
          program.setHeight(height);
        } else if (program.getEnd() >= tomorrow) {
          long duration = program.getDuration() - (program.getEnd() - tomorrow);
          int height = (2 * (int) duration / 60);
          program.setHeight(height);
        } else {
          long duration = program.getDuration();
          int height = (2 * (int) duration / 60);
          program.setHeight(height);
        }

      });
    });
    model.addAttribute("listOfProgramList", listOfProgramList);
    return "programs/list";
  }

  @PostMapping()
  String create(
      @Validated final ReservedProgram reservedProgram,
      final BindingResult bindingResult,
      final Model model
  ) {
    if (bindingResult.hasErrors()) {
      return read(model);
    }
    log.info("reservation -> {}", reservedProgram.toString());
    // TODO: ->  service
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
    //recorderService.create(createdReservedProgram);
    recorderActivity.create(createdReservedProgram);

    final String referer = httpServletRequest.getHeader("Referer");
    return "redirect:" + referer;
  }

  private List<Integer> getHourList() {
    final Instant instant = Instant.ofEpochMilli(new Date().getTime());
    final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    final int hour = zonedDateTime.getHour();
    final List<Integer> hourList = new ArrayList<>();
    for (int i = hour; i < hour + 24; i++) {
      if (i > 23) {
        hourList.add(i - 24);
      } else {
        hourList.add(i);
      }
    }
    return hourList;
  }
}
