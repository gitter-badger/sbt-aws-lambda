package com.gilt.aws.lambda

import com.amazonaws.{AmazonServiceException, AmazonClientException}
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model._
import sbt._

private[lambda] object AwsLambda {
  def updateLambda(lambdaName: LambdaName, bucketId: S3BucketId, s3Key: S3Key): Result[UpdateFunctionCodeResult] = {
    try {
      val client = new AWSLambdaClient(AwsCredentials.provider)

      val request = {
        val r = new UpdateFunctionCodeRequest()
        r.setFunctionName(lambdaName.value)
        r.setS3Bucket(bucketId.value)
        r.setS3Key(s3Key.value)

        r
      }

      val updateResult = client.updateFunctionCode(request)

      println(s"Updated lambda ${updateResult.getFunctionArn}")
      Success(updateResult)
    }
    catch {
      case ex @ (_ : AmazonClientException |
                 _ : AmazonServiceException) =>
        Failure(ex)
    }
  }

  def createLambda(jar: File,
                   functionName: LambdaName,
                   handlerName: HandlerName,
                   roleName: RoleARN,
                   s3BucketId: S3BucketId): Result[CreateFunctionResult] = {
    try {
      val client = new AWSLambdaClient(AwsCredentials.provider)

      val request = {
        val r = new CreateFunctionRequest()
        r.setFunctionName(functionName.value)
        r.setHandler(handlerName.value)
        r.setRole(roleName.value)
        r.setRuntime(com.amazonaws.services.lambda.model.Runtime.Java8)

        val functionCode = {
          val c = new FunctionCode
          c.setS3Bucket(s3BucketId.value)
          c.setS3Key(jar.getName)
          c
        }

        r.setCode(functionCode)

        r
      }

      val createResult = client.createFunction(request)

      println(s"Created Lambda: ${createResult.getFunctionArn}")
      Success(createResult)
    }
    catch {
      case ex @ (_ : AmazonClientException |
                 _ : AmazonServiceException) =>
        Failure(ex)
    }
  }

}
