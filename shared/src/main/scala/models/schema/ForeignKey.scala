package models.schema

case class ForeignKey(
  name: String,
  targetTable: String,
  references: Seq[Reference]
)
