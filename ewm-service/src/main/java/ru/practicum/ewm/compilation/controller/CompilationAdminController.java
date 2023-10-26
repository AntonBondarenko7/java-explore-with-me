package ru.practicum.ewm.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.compilation.dto.NewCompilationRequestDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequestDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public CompilationDto saveCompilation(@Valid @RequestBody NewCompilationRequestDto newCompilationDto) {
        CompilationDto compilationDto = compilationService.saveCompilation(newCompilationDto);
        log.info("Добавлена новая подборка: {}", compilationDto);
        return compilationDto;
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationById(@PathVariable Long compId) {
        compilationService.deleteCompilationById(compId);
        log.info("Удалена подборка с id = {}", compId);
    }

    @PatchMapping("/{compId}")
    @Validated
    public CompilationDto updateCompilation(
            @PathVariable Long compId, @Valid @RequestBody UpdateCompilationRequestDto updateCompilationRequest) {
        CompilationDto compilationDto = compilationService.updateCompilation(compId, updateCompilationRequest);
        log.info("Обновлена информация о подборке с id = {}: {}.", compId, compilationDto);
        return compilationDto;
    }

}
