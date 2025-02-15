/*
 * Copyright 2017-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.cache;

import java.time.Duration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link RedisCacheWriter} provides low level access to Redis commands ({@code SET, SETNX, GET, EXPIRE,...}) used for
 * caching. <br />
 * The {@link RedisCacheWriter} may be shared by multiple cache implementations and is responsible for writing / reading
 * binary data to / from Redis. The implementation honors potential cache lock flags that might be set.
 * <p>
 * The default {@link RedisCacheWriter} implementation can be customized with {@link BatchStrategy} to tune performance
 * behavior.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.0
 */
public interface RedisCacheWriter extends CacheStatisticsProvider {

	/**
	 * Create new {@link RedisCacheWriter} without locking behavior.
	 *
	 * @param connectionFactory must not be {@literal null}.
	 * @return new instance of {@link DefaultRedisCacheWriter}.
	 */
	static RedisCacheWriter nonLockingRedisCacheWriter(RedisConnectionFactory connectionFactory) {
		return nonLockingRedisCacheWriter(connectionFactory, BatchStrategies.keys());
	}

	/**
	 * Create new {@link RedisCacheWriter} without locking behavior.
	 *
	 * @param connectionFactory must not be {@literal null}.
	 * @param batchStrategy must not be {@literal null}.
	 * @return new instance of {@link DefaultRedisCacheWriter}.
	 * @since 2.6
	 */
	static RedisCacheWriter nonLockingRedisCacheWriter(RedisConnectionFactory connectionFactory,
			BatchStrategy batchStrategy) {

		Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");
		Assert.notNull(batchStrategy, "BatchStrategy must not be null!");

		return new DefaultRedisCacheWriter(connectionFactory, batchStrategy);
	}

	/**
	 * Create new {@link RedisCacheWriter} with locking behavior.
	 *
	 * @param connectionFactory must not be {@literal null}.
	 * @return new instance of {@link DefaultRedisCacheWriter}.
	 */
	static RedisCacheWriter lockingRedisCacheWriter(RedisConnectionFactory connectionFactory) {
		return lockingRedisCacheWriter(connectionFactory, BatchStrategies.keys());
	}

	/**
	 * Create new {@link RedisCacheWriter} with locking behavior.
	 *
	 * @param connectionFactory must not be {@literal null}.
	 * @param batchStrategy must not be {@literal null}.
	 * @return new instance of {@link DefaultRedisCacheWriter}.
	 * @since 2.6
	 */
	static RedisCacheWriter lockingRedisCacheWriter(RedisConnectionFactory connectionFactory,
			BatchStrategy batchStrategy) {

		Assert.notNull(connectionFactory, "ConnectionFactory must not be null!");

		return new DefaultRedisCacheWriter(connectionFactory, Duration.ofMillis(50), batchStrategy);
	}

	/**
	 * Write the given key/value pair to Redis an set the expiration time if defined.
	 *
	 * @param name The cache name must not be {@literal null}.
	 * @param key The key for the cache entry. Must not be {@literal null}.
	 * @param value The value stored for the key. Must not be {@literal null}.
	 * @param ttl Optional expiration time. Can be {@literal null}.
	 */
	void put(String name, byte[] key, byte[] value, @Nullable Duration ttl);

	/**
	 * Get the binary value representation from Redis stored for the given key.
	 *
	 * @param name must not be {@literal null}.
	 * @param key must not be {@literal null}.
	 * @return {@literal null} if key does not exist.
	 */
	@Nullable
	byte[] get(String name, byte[] key);

	/**
	 * Write the given value to Redis if the key does not already exist.
	 *
	 * @param name The cache name must not be {@literal null}.
	 * @param key The key for the cache entry. Must not be {@literal null}.
	 * @param value The value stored for the key. Must not be {@literal null}.
	 * @param ttl Optional expiration time. Can be {@literal null}.
	 * @return {@literal null} if the value has been written, the value stored for the key if it already exists.
	 */
	@Nullable
	byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl);

	/**
	 * Remove the given key from Redis.
	 *
	 * @param name The cache name must not be {@literal null}.
	 * @param key The key for the cache entry. Must not be {@literal null}.
	 */
	void remove(String name, byte[] key);

	/**
	 * Remove all keys following the given pattern.
	 *
	 * @param name The cache name must not be {@literal null}.
	 * @param pattern The pattern for the keys to remove. Must not be {@literal null}.
	 */
	void clean(String name, byte[] pattern);

	/**
	 * Reset all statistics counters and gauges for this cache.
	 *
	 * @since 2.4
	 */
	void clearStatistics(String name);

	/**
	 * Obtain a {@link RedisCacheWriter} using the given {@link CacheStatisticsCollector} to collect metrics.
	 *
	 * @param cacheStatisticsCollector must not be {@literal null}.
	 * @return new instance of {@link RedisCacheWriter}.
	 */
	RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector);

}
