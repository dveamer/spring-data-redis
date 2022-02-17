/*
 * Copyright 2011-2022 the original author or authors.
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
package org.springframework.data.redis.support.collections;

import org.springframework.data.redis.ObjectFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Parameterized instance of Redis tests.
 *
 * @author Costin Leau
 */
public class RedisListIntegrationTests extends AbstractRedisListIntegrationTests<Object> {

	/**
	 * Constructs a new <code>RedisListTests</code> instance.
	 *
	 * @param factory
	 * @param connFactory
	 */
	public RedisListIntegrationTests(ObjectFactory<Object> factory, RedisTemplate<Object, Object> template) {
		super(factory, template);
	}

	RedisStore copyStore(RedisStore store) {
		return new DefaultRedisList<>(store.getKey(), store.getOperations());
	}

	AbstractRedisCollection<Object> createCollection() {
		String redisName = getClass().getName();
		return new DefaultRedisList<>(redisName, template);
	}
}
