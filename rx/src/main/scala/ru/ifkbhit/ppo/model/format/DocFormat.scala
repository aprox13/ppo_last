package ru.ifkbhit.ppo.model.format

import org.bson.Document

trait DocReader[T] {
  def read(document: Document): T
}

trait DocWriter[T] {
  def write(element: T): Document
}

trait DocFormat[T] extends DocReader[T] with DocWriter[T]
