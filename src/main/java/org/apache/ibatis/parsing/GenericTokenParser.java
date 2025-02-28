/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * 通用token解析器，处理#{}和${}参数
 * ${}解析使用地方如：配置文件解析、sql解析
 * #{}解析使用地方如：sql解析
 *
 * @author Clinton Begin
 * 
 */
public class GenericTokenParser {

  // 占位符的开始标记
  private final String openToken;
  // 占位符的结束标记
  private final String closeToken;
  // token处理器，TokenHandler 接口的实现会按照一定的逻辑解析占位符
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    StringBuilder builder = new StringBuilder();
    // 检测 text 是否为空
    if (text != null && text.length() > 0) {
      char[] src = text.toCharArray();
      int offset = 0;
      // 查找开始标记
      int start = text.indexOf(openToken, offset);
      //#{favouriteSection,jdbcType=VARCHAR}
      //这里是循环解析参数，参考GenericTokenParserTest,比如可以解析${first_name} ${initial} ${last_name} reporting.这样的字符串,里面有3个 ${}
      while (start > -1) {
        //判断一下 ${ 前面是否是反斜杠，这个逻辑在老版的mybatis中（如3.1.0）是没有的
        if (start > 0 && src[start - 1] == '\\') {
          // the variable is escaped. remove the backslash.
      	  //新版已经没有调用substring了，改为调用如下的offset方式，提高了效率
          //issue #760
          // 遇到转移的开始标记，则直接将前面的字符串以及开始标记追加到 builder 中
          builder.append(src, offset, start - offset - 1).append(openToken);
          offset = start + openToken.length();
        } else {
          int end = text.indexOf(closeToken, start);
          if (end == -1) {
            builder.append(src, offset, src.length - offset);
            offset = src.length;
          } else {
            builder.append(src, offset, start - offset);
            offset = start + openToken.length();
            String content = new String(src, offset, end - offset);
            //得到一对大括号里的字符串后，调用handler.handleToken,比如替换变量这种功能
            builder.append(handler.handleToken(content));
            offset = end + closeToken.length();
          }
        }
        start = text.indexOf(openToken, offset);
      }
      if (offset < src.length) {
        builder.append(src, offset, src.length - offset);
      }
    }
    return builder.toString();
  }

}
