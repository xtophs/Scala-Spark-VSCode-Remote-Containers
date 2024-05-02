import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import com.github.mrpowers.spark.fast.tests.DataFrameComparer

class CubeCalculatorTest extends org.scalatest.funsuite.AnyFunSuite with DataFrameComparer{
  
  val SPARK_MASTER = "spark.master"
  val DEFAULT_MASTER = "local[*]"

  // get default conf
  val defaultConf = new SparkConf()
  
  // test("CubeCalculator.cube") {
  //   assert(CubeCalculator.cube(3) === 27)
  // }

  // test("FabricRead.readFile"){
  //   assert(FabricRead.readFile())
  // }

  test("FabricRead.readTable"){
    assert(FabricRead.readTable())
  }
  test("MrPowersTest")
  {
    // if master is not defined then set default to local
    if(!defaultConf.contains(SPARK_MASTER)) defaultConf.setMaster(DEFAULT_MASTER)

    val spark = SparkSession
      .builder()
      .config(defaultConf)
      .appName("RandomForestClassifierExample")
      .getOrCreate()

    import spark.implicits._
  
    val sourceDF = Seq(
      (1, "word"),
      (5, "word")
    ).toDF("number", "word")

    val expectedDF = Seq(
      (1, "word"),
      (5, "word")
    ).toDF("number", "word")

    assertSmallDataFrameEquality(sourceDF, expectedDF)

    spark.close()
  }
}
