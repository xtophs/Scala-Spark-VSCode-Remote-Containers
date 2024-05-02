import com.azure.storage.file.datalake.{DataLakeDirectoryClient, DataLakeFileClient, DataLakeFileSystemClient}
import com.azure.storage.file.datalake._
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.identity.ClientSecretCredentialBuilder
import java.io.ByteArrayOutputStream
import org.apache.log4j.{Level, Logger, PatternLayout, ConsoleAppender}
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf

object FabricRead extends App {

    val SPARK_MASTER = "spark.master"
    val DEFAULT_MASTER = "local[*]"

    def configureLogging(): Unit = {
        // Configure Log4j
        val rootLogger = Logger.getRootLogger
        rootLogger.setLevel(Level.DEBUG)
        val layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n")
        val appender = new ConsoleAppender(layout)
        rootLogger.addAppender(appender)
    }

    def readEnvFile(envFile: String): Map[String, String] = {
        scala.io.Source.fromFile(envFile).getLines().filter(_.nonEmpty).map { line =>
            println(line)
            val Array(key, value) = line.split("=", 2)
            key -> value
        }.toMap
    }

    def configureCredentials(env: Map[String, String]): (String, String, String) = {
        val clientId = env.getOrElse("CLIENT_ID", "")
        val clientSecret = env.getOrElse("CLIENT_SECRET", "")
        val tenantId = env.getOrElse("TENANT_ID", "")
        (clientId, clientSecret, tenantId)
    }

    def configurePaths(env: Map[String, String]): (String, String, String, String) = {
        val onelakePath = env.getOrElse("ONELAKE_URL", "")
        val fileSystemName = env.getOrElse("FILE_SYSTEM_NAME", "")
        val directoryPath = env.getOrElse("DIRECTORY_PATH", "")
        val fileName = env.getOrElse("FILE_NAME", "")
        (onelakePath, fileSystemName, directoryPath, fileName)
    }

    def printEnvironmentVariables(env: Map[String, String]): Unit = {
        env.foreach { case (key, value) =>
            println(s"$key: $value")
        }
    }

    def readFile(): Boolean = {
        configureLogging()

        val envFile = ".env"
        val env = readEnvFile(envFile)
        val (clientId, clientSecret, tenantId) = configureCredentials(env)
        val (onelakePath, fileSystemName, directoryPath, fileName) = configurePaths(env)
        printEnvironmentVariables(env)

        // Create a DataLakeFileSystemClient using the default Azure credentials
        val credential = new ClientSecretCredentialBuilder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .tenantId(tenantId)
            .build()

        println("Got credentials")

        // Get a service client object
        val serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(onelakePath)
            .credential(credential)
            .buildClient()

        println("Got serviceClient")

        // Show available file systems
        val fileSystems = serviceClient.listFileSystems()
        fileSystems.forEach(fileSystem => println(fileSystem.getName()))

        // Get a DataLakeDirectoryClient for the specified directory
        val fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(onelakePath + "/" + fileSystemName)
            .credential(credential)
            .buildClient()

        println("Got fileSystemClient")

        val directoryClient = fileSystemClient.getDirectoryClient(directoryPath)
        println("Got directoryClient")
        println(s"Directory exists: ${directoryClient.exists()}")

        // Get a DataLakeFileClient for the specified file
        val fileClient = directoryClient.getFileClient(fileName)
        val inMemoryStream = new ByteArrayOutputStream()

        // Read the contents of the file
        println("Reading file")
        val fileContents = fileClient.read(inMemoryStream)

        // Print length of file
        println(s"File: ${fileContents}")
        println(s"File length: ${inMemoryStream.size()}")

        true
    }

    def readTable(): Boolean = {
        configureLogging()
        val envFile = ".env"
        val env = readEnvFile(envFile)
        val (clientId, clientSecret, tenantId) = configureCredentials(env)
        val (onelakePath, _, _, _) = configurePaths(env)
        printEnvironmentVariables(env)

        val defaultConf = new SparkConf()
        if(!defaultConf.contains(SPARK_MASTER)) defaultConf.setMaster(DEFAULT_MASTER)
        val spark = SparkSession.builder
            .appName("My App")
            .config(defaultConf)
            .getOrCreate()
        spark.sparkContext.setLogLevel("WARN")

        // val spark = SparkSession
        //     .builder()
        //     .config(new SparkConf())
        //     .config("fs.azure.account.oauth2.client.endpoint", s"https://login.microsoftonline.com/1a3fbda0-40c8-437f-9d41-d6ebb12194c6/oauth2/token")
        //     .config("fs.azure.account.auth.type", "OAuth")
        //     .config("fs.azure.account.oauth.provider.type", "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider")
        //     .config("fs.azure.account.oauth2.client.id", clientId)
        //     .config("fs.azure.account.oauth2.client.secret", clientSecret)
        //     .master("local")
        //     .appName("CogniteFabricExample")
        //     .getOrCreate()

        // spark.conf.set("fs.azure.account.auth.type", "OAuth")
        // spark.conf.set("fs.azure.account.oauth.provider.type", "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider")
        // spark.conf.set("fs.azure.account.oauth2.client.id", clientId)
        // spark.conf.set("fs.azure.account.oauth2.client.secret", clientSecret)
        // spark.conf.set("fs.azure.account.oauth2.client.endpoint", s"https://login.microsoftonline.com/$tenantId/oauth2/token")
        val oneLakePath = s"abfss://FabricCogniteDemo@onelake.dfs.fabric.microsoft.com/CogniteLakehouse.Lakehouse"
        val df = spark.read.format("delta").load(oneLakePath + "/Tables/Assets")
        println(s"Count: ${df.count()}")

        return true
    }

}
