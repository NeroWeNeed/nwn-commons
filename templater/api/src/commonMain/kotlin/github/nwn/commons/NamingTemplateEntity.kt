package github.nwn.commons

interface NamingTemplateEntity<T : Any> {
    fun getProperty(data: T,propertyName: String) : Any?
}