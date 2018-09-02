package com.youzan.spring.nsq.annotation;

import com.youzan.spring.nsq.core.MessageListenerContainerFactory;

import org.springframework.context.annotation.Import;

/**
 * Enable nsq listener annotated endpoints that are created under the covers by a {@link
 * MessageListenerContainerFactory NsqListenerContainerFactory}.
 * To be used on {@link org.springframework.context.annotation.Configuration Configuration} classes
 * as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableNsq
 * public class AppConfig {
 * 	&#064;Bean
 * 	public ConcurrentNsqListenerContainerFactory myNsqListenerContainerFactory() {
 * 		ConcurrentNsqListenerContainerFactory factory = new ConcurrentNsqListenerContainerFactory();
 * 		factory.setConsumerFactory(consumerFactory());
 * 		factory.setConcurrency(4);
 * 		return factory;
 *        }
 * 	// other &#064;Bean definitions
 * }
 * </pre>
 *
 * The {@code NsqListenerContainerFactory} is responsible to create the listener container for a
 * particular endpoint. Typical implementations, as the {@link MessageListenerContainerFactory
 * NsqListenerContainerFactory} used in the sample above, provides the necessary
 * configuration options that are supported by the underlying {@link
 * com.youzan.spring.nsq.listener.MessageListenerContainer MessageListenerContainer}.
 *
 * <p>
 * {@code @EnableNsq} enables detection of {@link NsqListener} annotations on any Spring-managed
 * bean in the container. For example, given a class {@code MyService}:
 *
 * <pre class="code">
 * package com.acme.foo;
 *
 * public class MyService {
 * 	&#064;NsqListener(containerFactory = "myNsqListenerContainerFactory", topics = "myTopic")
 * 	public void process(String msg) {
 * 		// process incoming message
 *        }
 * }
 * </pre>
 *
 * The container factory to use is identified by the {@link NsqListener#containerFactory()
 * containerFactory} attribute defining the name of the {@code NsqListenerContainerFactory} bean to
 * use. When none is set a {@code NsqListenerContainerFactory} bean with name {@code
 * NsqListenerContainerFactory} is assumed to be present.
 *
 * <p>
 * the following configuration would ensure that every time a message is received from topic
 * "myQueue", {@code MyService.process()} is called with the content of the message:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableNsq
 * public class AppConfig {
 * 	&#064;Bean
 * 	public MyService myService() {
 * 		return new MyService();
 *        }
 *
 * 	// Nsq infrastructure setup
 * }
 * </pre>
 *
 * Alternatively, if {@code MyService} were annotated with {@code @Component}, the following
 * configuration would ensure that its {@code @NsqListener} annotated method is invoked with a
 * matching incoming message:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableNsq
 * &#064;ComponentScan(basePackages = "com.acme.foo")
 * public class AppConfig {
 * }
 * </pre>
 *
 * Note that the created containers are not registered with the application context but can be
 * easily located for management purposes using the {@link com.youzan.spring.nsq.config.NsqListenerEndpointRegistry
 * NsqListenerEndpointRegistry}.
 *
 * <p>
 * Annotated methods can use a flexible signature; in particular, it is possible to use the {@link
 * org.springframework.messaging.Message Message} abstraction and related annotations, see {@link
 * NsqListener} Javadoc for more details. For instance, the following would inject the content of
 * the message and the Nsq partition header:
 *
 * <pre class="code">
 * &#064;NsqListener(containerFactory = "myNsqListenerContainerFactory", topics = "myTopic")
 * public void process(String msg, @Header("Nsq_partition") int partition) {
 * 	// process incoming message
 * }
 * </pre>
 *
 * These features are abstracted by the {@link org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory
 * MessageHandlerMethodFactory} that is responsible to build the necessary invoker to process the
 * annotated method. By default, {@link org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory
 * DefaultMessageHandlerMethodFactory} is used.
 *
 * <p>
 * When more control is desired, a {@code @Configuration} class may implement {@link
 * NsqListenerConfigurer}. This allows access to the underlying {@link
 * com.youzan.spring.nsq.config.NsqListenerEndpointRegistrar NsqListenerEndpointRegistrar} instance.
 * The following example demonstrates how to specify an explicit default {@code
 * NsqListenerContainerFactory}
 *
 * <pre class="code">
 * {
 * 	&#64;code
 * 	&#064;Configuration
 * 	&#064;EnableNsq
 * 	public class AppConfig implements NsqListenerConfigurer {
 * 		&#064;Override
 * 		public void configureNsqListeners(NsqListenerEndpointRegistrar registrar) {
 * 			registrar.setContainerFactory(myNsqListenerContainerFactory());
 *                }
 *
 * 		&#064;Bean
 * 		public NsqListenerContainerFactory&lt;?, ?&gt; myNsqListenerContainerFactory() {
 * 			// factory settings
 *                }
 *
 * 		&#064;Bean
 * 		public MyService myService() {
 * 			return new MyService();
 *                }
 *        }
 * }
 * </pre>
 *
 * It is also possible to specify a custom {@link com.youzan.spring.nsq.config.NsqListenerEndpointRegistry
 * NsqListenerEndpointRegistry} in case you need more control on the way the containers are created
 * and managed. The example below also demonstrates how to customize the {@link
 * org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory} to
 * use with a custom {@link org.springframework.validation.Validator Validator} so that payloads
 * annotated with {@link org.springframework.validation.annotation.Validated Validated} are first
 * validated against a custom {@code Validator}.
 *
 * <pre class="code">
 * {
 * 	&#64;code
 * 	&#064;Configuration
 * 	&#064;EnableNsq
 * 	public class AppConfig implements NsqListenerConfigurer {
 * 		&#064;Override
 * 		public void configureNsqListeners(NsqListenerEndpointRegistrar registrar) {
 * 			registrar.setEndpointRegistry(myNsqListenerEndpointRegistry());
 * 			registrar.setMessageHandlerMethodFactory(myMessageHandlerMethodFactory);
 *                }
 *
 * 		&#064;Bean
 * 		public NsqListenerEndpointRegistry myNsqListenerEndpointRegistry() {
 * 			// registry configuration
 *                }
 *
 * 		&#064;Bean
 * 		public MessageHandlerMethodFactory myMessageHandlerMethodFactory() {
 * 			DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
 * 			factory.setValidator(new MyValidator());
 * 			return factory;
 *                }
 *
 * 		&#064;Bean
 * 		public MyService myService() {
 * 			return new MyService();
 *                }
 *        }
 * }
 * </pre>
 *
 * Implementing {@code NsqListenerConfigurer} also allows for fine-grained control over endpoints
 * registration via the {@code NsqListenerEndpointRegistrar}. For example, the following configures
 * an extra endpoint:
 *
 * <pre class="code">
 * {
 * 	&#64;code
 * 	&#064;Configuration
 * 	&#064;EnableNsq
 * 	public class AppConfig implements NsqListenerConfigurer {
 * 		&#064;Override
 * 		public void configureNsqListeners(NsqListenerEndpointRegistrar registrar) {
 * 			SimpleNsqListenerEndpoint myEndpoint = new SimpleNsqListenerEndpoint();
 * 			// ... configure the endpoint
 * 			registrar.registerEndpoint(endpoint, anotherNsqListenerContainerFactory());
 *                }
 *
 * 		&#064;Bean
 * 		public MyService myService() {
 * 			return new MyService();
 *                }
 *
 * 		&#064;Bean
 * 		public NsqListenerContainerFactory&lt;?, ?&gt; anotherNsqListenerContainerFactory() {
 * 			// ...
 *                }
 *
 * 		// nsq infrastructure setup
 *        }
 * }
 * </pre>
 *
 * Note that all beans implementing {@code NsqListenerConfigurer} will be detected and invoked in a
 * similar fashion. The example above can be translated in a regular bean definition registered in
 * the context in case you use the XML configuration.
 *
 * @author: clong
 * @date: 2018-08-29
 * @see NsqListener
 * @see NsqListenerAnnotationBeanPostProcessor
 * @see com.youzan.spring.nsq.config.NsqListenerEndpointRegistrar
 * @see com.youzan.spring.nsq.config.NsqListenerEndpointRegistry
 */
@Import(NsqBootstrapConfiguration.class)
public @interface EnableNsq {

}
