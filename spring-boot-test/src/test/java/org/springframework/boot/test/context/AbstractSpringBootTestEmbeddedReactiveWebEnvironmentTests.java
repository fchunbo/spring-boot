/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.context;

import org.junit.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.ReactiveWebApplicationContext;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.context.embedded.ReactiveWebServerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for {@link SpringBootTest} tests configured to start an embedded reactive
 * container.
 *
 * @author Stephane Nicoll
 */
public abstract class AbstractSpringBootTestEmbeddedReactiveWebEnvironmentTests {

	@LocalServerPort
	private int port = 0;

	@Value("${value}")
	private int value = 0;

	@Autowired
	private ReactiveWebApplicationContext context;

	@Autowired
	private WebTestClient webClient;

	public ReactiveWebApplicationContext getContext() {
		return this.context;
	}

	@Test
	public void runAndTestHttpEndpoint() {
		assertThat(this.port).isNotEqualTo(8080).isNotEqualTo(0);
		WebTestClient.bindToServer().baseUrl("http://localhost:" + this.port).build()
				.get().uri("/").exchange().expectBody(String.class).value()
				.isEqualTo("Hello World");
	}

	@Test
	public void injectWebTestClient() {
		this.webClient.get().uri("/").exchange().expectBody(String.class).value()
				.isEqualTo("Hello World");
	}

	@Test
	public void annotationAttributesOverridePropertiesFile() throws Exception {
		assertThat(this.value).isEqualTo(123);
	}

	protected static class AbstractConfig {

		@Value("${server.port:8080}")
		private int port = 8080;

		@Bean
		public HttpHandler httpHandler(ApplicationContext applicationContext) {
			return WebHttpHandlerBuilder.applicationContext(applicationContext).build();
		}

		@Bean
		public ReactiveWebServerFactory embeddedReactiveContainer() {
			TomcatReactiveWebServerFactory factory = new TomcatReactiveWebServerFactory();
			factory.setPort(this.port);
			return factory;
		}

		@Bean
		public static PropertySourcesPlaceholderConfigurer propertyPlaceholder() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@RequestMapping("/")
		public Mono<String> home() {
			return Mono.just("Hello World");
		}

	}

}
