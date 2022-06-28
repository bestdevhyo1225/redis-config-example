package com.example.redisconfiguration.repository

import com.example.redisconfiguration.config.RedisKey
import com.example.redisconfiguration.config.RedisTestConfig
import com.example.redisconfiguration.domain.Member
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ContextConfiguration
import java.util.concurrent.TimeUnit

@DataRedisTest
@EnableAutoConfiguration
@ContextConfiguration(classes = [RedisTestConfig::class, MemberRepository::class])
internal class MemberRepositoryTest : DescribeSpec() {

    override fun extensions(): List<Extension> = listOf(SpringExtension)
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String?>

    init {
        afterSpec {
            redisTemplate.delete(redisTemplate.keys("*"))
        }

        this.describe("set 메소드는") {
            it("Member를 저장한다.") {
                // given
                val id = 1L
                val key = RedisKey.getMemberKey(id = id)
                val value = Member.create(id = id, name = "Jang")
                val expireTime = 60L
                val timeUnit = TimeUnit.SECONDS

                // when
                memberRepository.set(key = key, value = value, expireTime = expireTime, timeUnit = timeUnit)

                // then
                val findValue = memberRepository.get(key = key, clazz = Member::class.java)

                findValue.shouldNotBeNull()
                findValue.id.shouldBe(value.id)
                findValue.name.shouldBe(value.name)
            }
        }

        this.describe("get 메소드는") {
            context("존재하지 않는 경우") {
                it("null 값을 반환한다.") {
                    // given
                    val key = RedisKey.getMemberKey(id = 1L)

                    // when
                    val value = memberRepository.get(key = key, clazz = Member::class.java)

                    // then
                    value.shouldBeNull()
                }
            }

            context("캐시가 만료 직전인 경우") {
                it("null 값을 반환한다.") {
                    // given
                    val id = 2L
                    val key = RedisKey.getMemberKey(id = id)
                    val value = Member.create(id = id, name = "Kang")
                    val expireTime = 100L
                    val timeUnit = TimeUnit.MILLISECONDS

                    memberRepository.set(key = key, value = value, expireTime = expireTime, timeUnit = timeUnit)

                    // when
                    val findValue = memberRepository.get(key = key, clazz = Member::class.java)

                    // then
                    findValue.shouldBeNull()
                }
            }
        }

        this.describe("setByPipeline 메소드는") {
            it("여러 개의 Key 그리고 Value 들을 한 번에 저장한다.") {
                // given
                val ids = listOf(1L, 2L, 3L, 4L, 5L)
                val keys = listOf(
                    RedisKey.getMemberKey(id = ids[0]),
                    RedisKey.getMemberKey(id = ids[1]),
                    RedisKey.getMemberKey(id = ids[2]),
                    RedisKey.getMemberKey(id = ids[3]),
                    RedisKey.getMemberKey(id = ids[4]),
                )
                val values = listOf(
                    Member.create(id = ids[0], name = "a"),
                    Member.create(id = ids[1], name = "b"),
                    Member.create(id = ids[2], name = "c"),
                    Member.create(id = ids[3], name = "d"),
                    Member.create(id = ids[4], name = "e"),
                )
                val keysAndValues = listOf(
                    Pair(first = keys[0], second = values[0]),
                    Pair(first = keys[1], second = values[1]),
                    Pair(first = keys[2], second = values[2]),
                    Pair(first = keys[3], second = values[3]),
                    Pair(first = keys[4], second = values[4]),
                )
                val expireTime = 60L

                // when
                memberRepository.setByPipeline(keysAndValues = keysAndValues, expireTime = expireTime)

                // then
                val findValue1 = memberRepository.get(key = keys[0], clazz = Member::class.java)
                val findValue2 = memberRepository.get(key = keys[1], clazz = Member::class.java)
                val findValue3 = memberRepository.get(key = keys[2], clazz = Member::class.java)
                val findValue4 = memberRepository.get(key = keys[3], clazz = Member::class.java)
                val findValue5 = memberRepository.get(key = keys[4], clazz = Member::class.java)

                findValue1.shouldNotBeNull()
                findValue1.id.shouldBe(values[0].id)
                findValue1.name.shouldBe(values[0].name)
                findValue2.shouldNotBeNull()
                findValue2.id.shouldBe(values[1].id)
                findValue2.name.shouldBe(values[1].name)
                findValue3.shouldNotBeNull()
                findValue3.id.shouldBe(values[2].id)
                findValue3.name.shouldBe(values[2].name)
                findValue4.shouldNotBeNull()
                findValue4.id.shouldBe(values[3].id)
                findValue4.name.shouldBe(values[3].name)
                findValue5.shouldNotBeNull()
                findValue5.id.shouldBe(values[4].id)
                findValue5.name.shouldBe(values[4].name)
            }
        }
    }
}
