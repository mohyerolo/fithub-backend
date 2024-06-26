package com.fithub.fithubbackend.domain.trainer.api;

import com.fithub.fithubbackend.domain.trainer.application.TrainerSearchService;
import com.fithub.fithubbackend.domain.trainer.dto.*;
import com.fithub.fithubbackend.global.exception.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "trainer's search (트레이너 조회)", description = "인증이 필요없는 트레이너 조회 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/search/trainers")
public class TrainerSearchController {

    private final TrainerSearchService trainerSearchService;

    @Operation(summary = "트레이너 검색(관심사, 검색 키워드, 성별). 사용할 검색 조건만 url 파라미터로 전달", parameters = {
            @Parameter(name = "interest", description = "1개의 관심사(PILATES, HEALTH, PT, CROSSFIT, YOGA)"),
            @Parameter(name = "keyword", description = "검색 키워드(트레이너 이름)"),
            @Parameter(name = "gender", description = "성별 (F, M)"),
            @Parameter(name = "pageable", description = "page(size = 9, sort = \"id\", desc 적용). 페이지 이동 시 page 값만 보내주면 됨. ex) \"page\" : 0 인 경우 1 페이지")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "검색 완료"),
    })
    @GetMapping
    public ResponseEntity<Page<TrainerOutlineDto>> searchTrainers(@ModelAttribute TrainerSearchFilterDto dto,
                                                                  @PageableDefault(size = 9, sort = "id",  direction = Sort.Direction.DESC)  Pageable pageable) {
        return ResponseEntity.ok(trainerSearchService.searchTrainers(dto, pageable));
    }

    @Operation(summary = "트레이너 조회 시 트레이너의 트레이닝에 작성된 모든 리뷰 조회", parameters = {
            @Parameter(name = "trainerId", description = "조회할 트레이너의 primary key(id)")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/reviews")
    public ResponseEntity<TrainerSearchAllReviewDto> getTrainerReviews (@RequestParam Long trainerId) {
        return ResponseEntity.ok(trainerSearchService.searchTrainerReviews(trainerId));
    }

    @Operation(summary = "트레이너 상세 조회 시 프로필 조회", parameters = {
            @Parameter(name = "trainerId", description = "조회할 트레이너의 primary key(id)")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 트레이너", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{trainerId}")
    public ResponseEntity<TrainerOutlineDto> getTrainerProfileInfo(@PathVariable Long trainerId) {
        return ResponseEntity.ok(trainerSearchService.searchTrainerProfileInfo(trainerId));
    }

    @Operation(summary = "트레이너 상세 조회 시 자격 사항 조회", parameters = {
            @Parameter(name = "trainerId", description = "조회할 트레이너의 primary key(id)")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/licenses")
    public ResponseEntity<TrainerSearchAllLicenseDto> getTrainerLicenses(@RequestParam Long trainerId) {
        return ResponseEntity.ok(trainerSearchService.searchTrainerLicenses(trainerId));
    }

    @Operation(summary = "트레이너 상세 조회 시 현재 근무 중인 회사 조회", description = "근무 중인 회사가 없는 경우 null 반환",
            parameters = {
                    @Parameter(name = "trainerId", description = "조회할 트레이너의 primary key(id)")
            }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/company")
    public ResponseEntity<TrainerSearchCompanyDto> getTrainerCompanyInfo(@RequestParam Long trainerId) {
        return ResponseEntity.ok(trainerSearchService.searchTrainerCompanyInfo(trainerId));
    }
}
