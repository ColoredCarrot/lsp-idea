package info.voidev.lspidea.util.xml

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Discriminator(
    val value: String,
)
