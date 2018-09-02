package com.youzan.spring.nsq.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * expression resolve component
 *
 * @author: clong
 * @date: 2018-09-01
 */
public class ExpressionResolver {

  private final BeanFactory beanFactory;
  private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();
  private BeanExpressionContext expressionContext;


  public ExpressionResolver(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;

    if (beanFactory instanceof ConfigurableListableBeanFactory) {
      ConfigurableListableBeanFactory cf = (ConfigurableListableBeanFactory) beanFactory;
      this.resolver = cf.getBeanExpressionResolver();
      this.expressionContext = new BeanExpressionContext(cf, null);
    }
  }


  @SuppressWarnings("unchecked")
  public void resolveAsString(Object resolvedValue, List<String> result) {
    if (resolvedValue instanceof String[]) {
      for (Object object : (String[]) resolvedValue) {
        resolveAsString(object, result);
      }
    }
    else if (resolvedValue instanceof String) {
      result.add((String) resolvedValue);
    }
    else if (resolvedValue instanceof Iterable) {
      for (Object object : (Iterable<Object>) resolvedValue) {
        resolveAsString(object, result);
      }
    }
    else {
      throw new IllegalArgumentException(String.format(
          "@NsqListener can't resolve '%s' as a String", resolvedValue));
    }
  }



  public String resolveExpressionAsString(String value, String attribute) {
    Object resolved = resolveExpression(value);
    if (resolved instanceof String) {
      return (String) resolved;
    } else {
      throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
                                      + "Resolved to [" + resolved.getClass() + "] for [" + value
                                      + "]");
    }
  }

  public int resolveExpressionAsInteger(String value, String attribute) {
    Object resolved = resolveExpression(value);
    if (resolved instanceof String) {
      return Integer.parseInt((String) resolved);
    } else if (resolved instanceof Number) {
      return ((Number) resolved).intValue();
    } else {
      throw new IllegalStateException(
          "The [" + attribute
          + "] must resolve to an Number or a String that can be parsed as an Integer. "
          + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
    }
  }

  public boolean resolveExpressionAsBoolean(String value, String attribute) {
    if (!StringUtils.hasText(value)) {
      return true;
    }

    Object resolved = resolveExpression(value);
    if (resolved instanceof Boolean) {
      return (Boolean) resolved;
    } else if (resolved instanceof String) {
      final String s = (String) resolved;
      return Boolean.parseBoolean(s);
    } else {
      throw new IllegalStateException(
          "The [" + attribute
          + "] must resolve to a Boolean or a String that can be parsed as a Boolean. "
          + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
    }
  }

  public Object resolveExpression(String value) {
    String resolvedValue = resolve(value);

    return this.resolver.evaluate(resolvedValue, this.expressionContext);
  }

  /**
   * Resolve the specified value if possible.
   *
   * @param value the value to resolve
   * @return the resolved value
   * @see ConfigurableBeanFactory#resolveEmbeddedValue
   */
  public String resolve(String value) {
    if (beanFactory instanceof ConfigurableBeanFactory) {
      return ((ConfigurableBeanFactory) beanFactory).resolveEmbeddedValue(value);
    }
    return value;
  }


}
