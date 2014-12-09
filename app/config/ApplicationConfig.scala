package config

import dao._
import controllers._
import org.springframework.scala.context.function.FunctionalConfiguration
import service.{PostService, CommentService}
import auth.SocialUserService

class ApplicationConfig extends FunctionalConfiguration {

  val commentDao = bean(){
    new CommentDao
  } destroy { 
    _.destroy()
  }

  val postDao = bean(){
    new PostDao
  } destroy {
    _.destroy()
  }

  val tagDao = bean(){
    new TagDao
  } destroy {
    _.destroy()
  }

  val commentService = bean(){
    new CommentService(commentDao())
  }

  val postService = bean(){
    new PostService(postDao(), tagDao())
  }

  val secureSocialUserService = bean(){
    new SocialUserService()
  }

  val runtimeEnvironment = bean("runtimeEnvironment"){
    new SpringRuntimeEnvironment(secureSocialUserService())
  }

  val customLoginController = bean(){
    implicit val runtimeEnv = runtimeEnvironment()
    new CustomLoginController
  }

  val postController = bean(){
    new PostController(postService(), runtimeEnvironment())
  }

  val imageController = bean(){
    implicit val runtimeEnv = runtimeEnvironment()
    new ImageController
  }

  val commentController = bean(){
    new CommentController(commentService(), runtimeEnvironment())
  }

  val tagController = bean(){
    new TagController(tagDao(), runtimeEnvironment())
  }

  val utilController = bean(){
    implicit val runtimeEnv = runtimeEnvironment()
    new UtilController
  }
}
