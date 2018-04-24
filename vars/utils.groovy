import hudson.model.*
import hudson.plugins.s3.*
import jenkins.model.*

def lastStableBuild(jobName, branchName) {
    def job = Jenkins.instance.itemMap.get(jobName)
    if (job == null) {
        throw new Exception("error no job found for ${jobName}")
    }
    def branch = job.getBranch(branchName)
    if (branch == null) {
        throw new Exception("error no job found for ${jobName}/${branchName}")
    }
    return branch.getLastStableBuild()
}

def resolveS3Artifact(parentName, branchName) {
    def run = lastStableBuild(parentName, branchName)
    if (run == null) {
        throw new Exception("error no run found for ${parentName}/${branchName}")
    }

    def action = run.getAction(S3ArtifactsAction.class)
    def artifact = action.getArtifacts()[0].getArtifact()
    if (artifact == null) {
        throw new Exception("error no artifact found for ${parentName}/${branchName}")
    }
    // this is the path the plugin uses when it stores it as an s3 artifact
    def s3_filename = "s3://${artifact.bucket}/jobs/${parentName}/${branchName}/${run.number}/${artifact.name}"

    return s3_filename
}

def allBranches(parentName) {
    def printErr = System.err.&println

    def parentJob = Jenkins.instance.itemMap.get(parentName)
    if (parentJob == null) {
        printErr "No job found for '${parentName}'"
        return []
    }
    def jobs = parentJob.getAllJobs()
    return jobs.collect {it.name}
}