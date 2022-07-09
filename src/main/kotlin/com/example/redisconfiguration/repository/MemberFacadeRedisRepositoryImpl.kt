package com.example.redisconfiguration.repository

import com.example.redisconfiguration.config.RedisExpireTime
import com.example.redisconfiguration.config.RedisKey
import com.example.redisconfiguration.domain.Member
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class MemberFacadeRedisRepositoryImpl(
    @Qualifier(value = "redisServerCount")
    private val redisServerCount: Int,
    private val memberRedisServer1Repository: MemberRedisServer1Repository,
    private val memberRedisServer2Repository: MemberRedisServer2Repository,
    private val memberRedisServer3Repository: MemberRedisServer3Repository,
) : MemberFacadeRedisRepository {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val minRedisServerCount = 1

    override fun setMemberCache(value: Member) {
        CoroutineScope(context = Dispatchers.IO).launch {
            val key = RedisKey.getMemberKey(id = value.id)
            setMemberCacheInRedisServer1(key = key, value = value)
            setMemberCacheInRedisServer2(key = key, value = value)
            setMemberCacheInRedisServer3(key = key, value = value)
        }
    }

    suspend fun setMemberCacheInRedisServer1(key: String, value: Member) {
        try {
            logger.info("set member cache in redis server-1")
            memberRedisServer1Repository.set(
                key = key,
                value = value,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }
    }

    suspend fun setMemberCacheInRedisServer2(key: String, value: Member) {
        try {
            logger.info("set member cache in redis server-2")
            memberRedisServer2Repository.set(
                key = key,
                value = value,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }
    }

    suspend fun setMemberCacheInRedisServer3(key: String, value: Member) {
        try {
            logger.info("set member cache in redis server-3")
            memberRedisServer3Repository.set(
                key = key,
                value = value,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }
    }

    override fun setMembersCache(values: List<Member>) {
        val keysAndValues = values.map {
            Pair(first = RedisKey.getMemberKey(id = it.id), second = Member.create(id = it.id, name = it.name))
        }

        runBlocking {
            launch(context = Dispatchers.IO) {
                logger.info("set members cache in redis server-1")
                setMembersCacheInRedisServer1(keysAndValues = keysAndValues)
            }
            launch(context = Dispatchers.IO) {
                logger.info("set members cache in redis server-2")
                setMembersCacheInRedisServer2(keysAndValues = keysAndValues)
            }
            launch(context = Dispatchers.IO) {
                logger.info("set members cache in redis server-3")
                setMembersCacheInRedisServer3(keysAndValues = keysAndValues)
            }
        }
    }

    suspend fun setMembersCacheInRedisServer1(keysAndValues: List<Pair<String, Member>>) {
        try {
            memberRedisServer1Repository.setUsingPipeline(
                keysAndValues = keysAndValues,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }
    }

    suspend fun setMembersCacheInRedisServer2(keysAndValues: List<Pair<String, Member>>) {
        try {
            memberRedisServer2Repository.setUsingPipeline(
                keysAndValues = keysAndValues,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }
    }

    suspend fun setMembersCacheInRedisServer3(keysAndValues: List<Pair<String, Member>>) {
        try {
            memberRedisServer3Repository.setUsingPipeline(
                keysAndValues = keysAndValues,
                expireTime = RedisExpireTime.MEMBER,
                timeUnit = TimeUnit.SECONDS
            )
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
        }
    }

    override fun getMemberCache(id: Long): Member? {
        val key = RedisKey.getMemberKey(id = id)
        val nodeIndex = getNodeIndex()

        logger.info("current nodeIndex: {}", nodeIndex)

        return try {
            when (nodeIndex) {
                0 -> memberRedisServer1Repository.get(key = key, clazz = Member::class.java)
                1 -> memberRedisServer2Repository.get(key = key, clazz = Member::class.java)
                2 -> memberRedisServer3Repository.get(key = key, clazz = Member::class.java)
                else -> throw RuntimeException("해당 노드 인덱스의 RedisTemplate를 추가하세요. (nodeIndex: $nodeIndex")
            }
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            Member.create(id = id, name = "redis connection failure fallback")
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            Member.create(id = id, name = "query timeout fallback")
        }
    }

    override fun getMembersCache(ids: List<Long>): List<Member?> {
        val keys = ids.map { RedisKey.getMemberKey(id = it) }
        val nodeIndex = getNodeIndex()

        logger.info("current nodeIndex: {}", nodeIndex)

        return try {
            when (nodeIndex) {
                0 -> memberRedisServer1Repository.getUsingPipeline(keys = keys, clazz = Member::class.java)
                1 -> memberRedisServer2Repository.getUsingPipeline(keys = keys, clazz = Member::class.java)
                2 -> memberRedisServer3Repository.getUsingPipeline(keys = keys, clazz = Member::class.java)
                else -> throw RuntimeException("해당 노드 인덱스의 RedisTemplate를 추가하세요. (nodeIndex: $nodeIndex")
            }
        } catch (exception: RedisConnectionFailureException) {
            logger.error("exception", exception)
            ids.map { Member.create(id = it, name = "redis connection failure fallback") }
        } catch (exception: QueryTimeoutException) {
            logger.error("exception", exception)
            ids.map { Member.create(id = it, name = "query timeout fallback") }
        }
    }

    private fun getNodeIndex(): Int {
        logger.info("redisServerCount: {}", redisServerCount)
        return ((minRedisServerCount..redisServerCount).random()).minus(1)
    }
}
