rootProject.name = "nome-app"
include("domain")
include("application")

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}
