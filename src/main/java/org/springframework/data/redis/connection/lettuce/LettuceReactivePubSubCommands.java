/*
 * Copyright 2018-2022 the original author or authors.
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
package org.springframework.data.redis.connection.lettuce;

import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.Function;

import org.reactivestreams.Publisher;

import org.springframework.data.redis.connection.ReactivePubSubCommands;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.connection.ReactiveSubscription.ChannelMessage;
import org.springframework.data.redis.connection.SubscriptionListener;
import org.springframework.util.Assert;

/**
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.1
 */
class LettuceReactivePubSubCommands implements ReactivePubSubCommands {

	private final LettuceReactiveRedisConnection connection;

	LettuceReactivePubSubCommands(LettuceReactiveRedisConnection connection) {
		this.connection = connection;
	}

	@Override
	public Mono<ReactiveSubscription> createSubscription(SubscriptionListener listener) {

		return connection.getPubSubConnection()
				.map(pubSubConnection -> new LettuceReactiveSubscription(listener, pubSubConnection,
						connection.translateException()));
	}

	@Override
	public Flux<Long> publish(Publisher<ChannelMessage<ByteBuffer, ByteBuffer>> messageStream) {

		Assert.notNull(messageStream, "ChannelMessage stream must not be null!");

		return connection.getCommands().flatMapMany(commands -> Flux.from(messageStream)
				.flatMap(message -> commands.publish(message.getChannel(), message.getMessage())));
	}

	@Override
	public Mono<Void> subscribe(ByteBuffer... channels) {

		Assert.notNull(channels, "Channels must not be null!");

		return doWithPubSub(commands -> commands.subscribe(channels));
	}

	@Override
	public Mono<Void> pSubscribe(ByteBuffer... patterns) {

		Assert.notNull(patterns, "Patterns must not be null!");

		return doWithPubSub(commands -> commands.psubscribe(patterns));
	}

	private <T> Mono<T> doWithPubSub(Function<RedisPubSubReactiveCommands<ByteBuffer, ByteBuffer>, Mono<T>> function) {

		return connection.getPubSubConnection().flatMap(pubSubConnection -> function.apply(pubSubConnection.reactive()))
				.onErrorMap(connection.translateException());
	}
}
