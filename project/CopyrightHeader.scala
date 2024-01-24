/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.grpc

import sbt._
import Keys._
import de.heikoseeberger.sbtheader.{ CommentCreator, HeaderPlugin, NewLine }
import org.apache.commons.lang3.StringUtils

object CopyrightHeader extends AutoPlugin {
  import HeaderPlugin.autoImport._

  override lazy val requires = HeaderPlugin
  override lazy val trigger = allRequirements

  override lazy val projectSettings =
    Def.settings(Seq(Compile, Test).flatMap { config =>
      inConfig(config)(
        Seq(
          headerLicense := Some(HeaderLicense.Custom(apacheHeader)),
          headerMappings := headerMappings.value ++ Map(
            HeaderFileType.scala -> cStyleComment,
            HeaderFileType.java -> cStyleComment,
            HeaderFileType("sbt") -> cStyleComment,
            HeaderFileType.conf -> hashLineComment,
            HeaderFileType("txt") -> twirlStyleBlockComment),
          (headerCreate / unmanagedResourceDirectories) += baseDirectory.value / "src" / "main" / "twirl"))
    })

  val apacheFromAkkaSourceHeader: String =
    """Licensed to the Apache Software Foundation (ASF) under one or more
      |license agreements; and to You under the Apache License, version 2.0:
      |
      |  https://www.apache.org/licenses/LICENSE-2.0
      |
      |This file is part of the Apache Pekko project, which was derived from Akka.
      |""".stripMargin

  val apacheHeader: String =
    """Licensed to the Apache Software Foundation (ASF) under one or more
      |contributor license agreements. See the NOTICE file distributed with
      |this work for additional information regarding copyright ownership.
      |The ASF licenses this file to You under the Apache License, Version 2.0
      |(the "License"); you may not use this file except in compliance with
      |the License. You may obtain a copy of the License at
      |
      |   http://www.apache.org/licenses/LICENSE-2.0
      |
      |Unless required by applicable law or agreed to in writing, software
      |distributed under the License is distributed on an "AS IS" BASIS,
      |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      |See the License for the specific language governing permissions and
      |limitations under the License.
      |""".stripMargin

  val apacheSpdxHeader: String = "SPDX-License-Identifier: Apache-2.0"

  val twirlStyleBlockComment = HeaderCommentStyle.twirlStyleBlockComment.copy(commentCreator = new CommentCreator() {

    override def apply(text: String, existingText: Option[String]): String = {
      val formatted = existingText match {
        case Some(currentText) if isApacheCopyrighted(currentText) || isGenerated(currentText) =>
          currentText
        case Some(currentText) if isOnlyLightbendCopyrightAnnotated(currentText) =>
          HeaderCommentStyle.twirlStyleBlockComment.commentCreator(apacheFromAkkaSourceHeader,
            existingText) + NewLine * 2 + currentText
        case Some(currentText) =>
          throw new IllegalStateException(s"Unable to detect copyright for header: [$currentText]")
        case None =>
          HeaderCommentStyle.twirlStyleBlockComment.commentCreator(text, existingText)
      }
      formatted.trim
    }
  })

  val cStyleComment = HeaderCommentStyle.cStyleBlockComment.copy(commentCreator = new CommentCreator() {

    override def apply(text: String, existingText: Option[String]): String = {
      val formatted = existingText match {
        case Some(currentText) if isApacheCopyrighted(currentText) || isGenerated(currentText) =>
          currentText
        case Some(currentText) if isOnlyLightbendCopyrightAnnotated(currentText) =>
          HeaderCommentStyle.cStyleBlockComment.commentCreator(text, existingText) + NewLine * 2 + currentText
        case Some(currentText) =>
          throw new IllegalStateException(s"Unable to detect copyright for header: [$currentText]")
        case None =>
          HeaderCommentStyle.cStyleBlockComment.commentCreator(text, existingText)
      }
      formatted.trim
    }
  })

  val hashLineComment = HeaderCommentStyle.hashLineComment.copy(commentCreator = new CommentCreator() {

    // deliberately hardcode use of apacheSpdxHeader and ignore input text
    override def apply(text: String, existingText: Option[String]): String = {
      val formatted = existingText match {
        case Some(currentText) if isApacheCopyrighted(currentText) || isGenerated(currentText) =>
          currentText
        case Some(currentText) if isOnlyLightbendCopyrightAnnotated(currentText) =>
          HeaderCommentStyle.hashLineComment.commentCreator(apacheSpdxHeader, existingText) + NewLine * 2 + currentText
        case Some(currentText) =>
          throw new IllegalStateException(s"Unable to detect copyright for header: [$currentText]")
        case None =>
          HeaderCommentStyle.hashLineComment.commentCreator(apacheSpdxHeader, existingText)
      }
      formatted.trim
    }
  })

  private def isGenerated(text: String): Boolean =
    StringUtils.contains(text, "DO NOT EDIT DIRECTLY")

  private def isApacheCopyrighted(text: String): Boolean =
    StringUtils.containsIgnoreCase(text, "licensed to the apache software foundation (asf)") ||
    StringUtils.containsIgnoreCase(text, "www.apache.org/licenses/license-2.0") ||
    StringUtils.contains(text, "Apache-2.0")

  private def isLightbendCopyrighted(text: String): Boolean =
    StringUtils.containsIgnoreCase(text, "lightbend inc.")

  private def isOnlyLightbendCopyrightAnnotated(text: String): Boolean =
    isLightbendCopyrighted(text) && !isApacheCopyrighted(text)
}
