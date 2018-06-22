package knub

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.dao.*
import com.zaxxer.hikari.*

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}

object Cities: IntIdTable() {
    val name = varchar("name", 50)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var city by City referencedOn Users.city
    var age by Users.age
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
    val users by User referrersOn Users.city
}


fun connectToPostgres() {
//    val hikariConfig = HikariConfig()
//    val credentialsAndConnectionString = System.getenv("DATABASE_URL").split("@")
//    val credentials = credentialsAndConnectionString[0].split("postgres://")[1].split(":")
//    val connectionString = credentialsAndConnectionString[1]
//    hikariConfig.jdbcUrl = "jdbc:postgresql://$connectionString?sslmode=require"
//    hikariConfig.driverClassName = "org.postgresql.Driver"
//    hikariConfig.username = System.getenv("DATABASE_USER")
//    hikariConfig.password = System.getenv("DATABASE_PASSWORD")
//    val config = HikariConfig("/hikari.properties")
//    val ds = HikariDataSource(config)
//    val ds = HikariDataSource(hikariConfig)
//    println(hikariConfig.jdbcUrl)
//    postgres://zivtmpbecxrbbc:4e3c9c5dbde66e05dd978f5d955bb79575dc6e87e69d196d06d546839f4e5454@ec2-54-228-181-43.eu-west-1.compute.amazonaws.com:5432/d4tldfuc5r8pgj
    val url = "jdbc:postgresql://${System.getenv("DATABASE_HOST")}:${System.getenv("DATABASE_PORT")}/${System.getenv("DATABASE_NAME")}"
    println(url)
    Database.connect(url, driver = "org.postgresql.Driver", user = System.getenv("DATABASE_USER"), password = System.getenv("DATABASE_PASSWORD"))
//    Database.connect(ds)
//    Database.connect("jdbc:${System.getenv("DATABASE_URL").replace(":postgres://", ":postgresql://")}", driver = "org.postgresql.Driver")
}

fun Application.main() {
//    Database.connect("jdbc:postgresql:postgres", driver = "org.postgresql.Driver", user = System.env("DATABASE_USER"), password = System.env("DATABASE_PASSWORD"))
//    Database.connect(System.getenv("DATABASE_URL"), driver = "org.postgresql.Driver", user = System.getenv("DATABASE_USER"), password = System.getenv("DATABASE_PASSWORD"))
    connectToPostgres()
    install(DefaultHeaders)
    install(CallLogging)
    routing {
//        get("/") {
//            call.respondHtml {
//                head {
//                    title { +"Ktor: Docker" }
//                }
//                body {
//                    div {
//                        p {
//                            +System.getenv("DATABASE_URL")
//                        }
//                    }
//                }
//            }
//        }
        get("/") {
            transaction {
                create(Cities, Users)

                val stPete = City.new {
                    name = "St. Petersburg"
                }

                val munich = City.new {
                    name = "Munich"
                }

                User.new {
                    name = "a"
                    city = stPete
                    age = 5
                }

                User.new {
                    name = "b"
                    city = stPete
                    age = 27
                }

                User.new {
                    name = "c"
                    city = munich
                    age = 42
                }

                println("Cities: ${City.all().joinToString { it.name }}")
                println("Users in ${stPete.name}: ${stPete.users.joinToString { it.name }}")
                println("Adults: ${User.find { Users.age greaterEq 18 }.joinToString { it.name }}")
            }
            call.respondHtml {
                head {
                    title { +"Ktor: Docker" }
                }
                body {
                    val runtime = Runtime.getRuntime()
                    p { +"Hello from Ktor Netty engine running in Docker sample application" }
                    p { +"Runtime.getRuntime().availableProcessors(): ${runtime.availableProcessors()}" }
                    p { +"Runtime.getRuntime().freeMemory(): ${runtime.freeMemory()}" }
                    p { +"Runtime.getRuntime().totalMemory(): ${runtime.totalMemory()}" }
                    p { +"Runtime.getRuntime().maxMemory(): ${runtime.maxMemory()}" }
                    div {
                        transaction {
                            City.all().forEach {
                                p {
                                    +it.name
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
