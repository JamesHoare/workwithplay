package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

object ContactType extends Enumeration {
  type ContactType = Value
  
  val Email, Telephone, Address = Value
}

case class Contact(id: Pk[Int], contactType: ContactType.ContactType, contact: String)

class ContactManager {
  def save(user: User, contact: Contact): Int = {
    val id: Option[Long] = DB.withConnection { implicit connection =>
      SQL(""" 
    		  INSERT INTO Contact(contactType, contact, userId) 
    		  VALUES({contactType}, {contact}, {userId})
      """).on(
        'contactType -> contact.contactType.toString(),
        'contact -> contact.contact,
        'userId -> user.id).executeInsert()
    }
    id.get.toInt
  }

  private val contactParser: RowParser[Contact] = {
    get[Pk[Int]]("id") ~
      get[String]("contactType") ~
      get[String]("contact") map {
        case id ~ contactType ~ contact => Contact(id, ContactType.withName(contactType), contact)
      }
  }

  def loadByUserId(userId: Int): List[Contact] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from Contact where userId = {userId}")
        .on('userId -> userId)
        .as(contactParser *)
    }
  }

  def load(id: Int): Option[Contact] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from Contact WHERE id = {id}")
        .on('id -> id)
        .as(contactParser.singleOpt)
    }
  }
  
  def update(id: Int, contact: Contact) {
    DB.withConnection { implicit connection =>
      SQL(""" 
    		  UPDATE Contact SET
    		  contactType = {contactType},
    		  contact = {contact}
    		  WHERE id = {id}
      """).on(
        'id -> id,
        'contactType -> contact.contactType.toString(),
        'contact -> contact.contact).executeUpdate
    }
  }
  
  def delete(id: Int) {
    DB.withConnection { implicit connection =>
      SQL(""" 
          DELETE FROM Contact where id = {id}
      """).on(
        'id -> id).executeUpdate
    }
  }
}