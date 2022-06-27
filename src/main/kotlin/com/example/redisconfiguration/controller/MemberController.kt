package com.example.redisconfiguration.controller

import com.example.redisconfiguration.controller.request.CreateMemberCache
import com.example.redisconfiguration.controller.response.SuccessResponse
import com.example.redisconfiguration.service.MemberService
import com.example.redisconfiguration.service.dto.CreateMemberCacheResult
import com.example.redisconfiguration.service.dto.FindMemberCacheResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/redis")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun set(@RequestBody request: CreateMemberCache): ResponseEntity<SuccessResponse<CreateMemberCacheResult>> {
        val createMemberCacheResult = memberService.set(id = request.id, name = request.name)
        return ResponseEntity.ok(SuccessResponse(data = createMemberCacheResult))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<SuccessResponse<FindMemberCacheResult>> {
        val findMemberCacheResult = memberService.get(id = id)
        return ResponseEntity.ok(SuccessResponse(data = findMemberCacheResult))
    }
}
