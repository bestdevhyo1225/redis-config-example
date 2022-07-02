package com.example.redisconfiguration.controller

import com.example.redisconfiguration.controller.request.CreateMemberCacheRequest
import com.example.redisconfiguration.controller.request.CreateMemberCachesRequest
import com.example.redisconfiguration.controller.response.SuccessResponse
import com.example.redisconfiguration.service.MemberService
import com.example.redisconfiguration.service.dto.CreateMemberCacheDto
import com.example.redisconfiguration.service.dto.CreateMemberCacheResultDto
import com.example.redisconfiguration.service.dto.FindMemberCacheResultDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/redis")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun set(@RequestBody request: CreateMemberCacheRequest): ResponseEntity<SuccessResponse<CreateMemberCacheResultDto>> {
        val createMemberCacheResult = memberService.set(id = request.id, name = request.name)
        return ResponseEntity.ok(SuccessResponse(data = createMemberCacheResult))
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    fun setUsingPipeline(
        @RequestBody request: CreateMemberCachesRequest
    ): ResponseEntity<SuccessResponse<List<CreateMemberCacheResultDto>>> {
        val dtos = request.members.map { CreateMemberCacheDto(id = it.id, name = it.name) }
        val createMemberCacheResults = memberService.setUsingPipeline(dtos = dtos)
        return ResponseEntity.ok(SuccessResponse(data = createMemberCacheResults))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<SuccessResponse<FindMemberCacheResultDto>> {
        val findMemberCacheResult = memberService.get(id = id)
        return ResponseEntity.ok(SuccessResponse(data = findMemberCacheResult))
    }

    @GetMapping
    fun getUsingPipeline(
        @RequestParam(value = "start") start: Int,
        @RequestParam(value = "count") count: Int
    ): ResponseEntity<SuccessResponse<List<FindMemberCacheResultDto>>> {
        val findMemberCacheResultDtos = memberService.getUsingPipeline(start = start, count = count)
        return ResponseEntity.ok(SuccessResponse(data = findMemberCacheResultDtos))
    }
}
