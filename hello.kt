import konan.worker.TransferMode
import konan.worker.freeze
import konan.worker.startWorker
import platform.darwin.glob

fun main(args: Array<String>) {
    freezeLocal()
}

fun simpleWorker(){
    val worker = startWorker()

    worker.schedule(TransferMode.CHECKED, {}){
        println("I'm working!")
    }.consume {  }

    worker.requestTermination()
}

fun failedReference(){
    val worker = startWorker()

    val dat = SomeData("data")
    worker.schedule(TransferMode.CHECKED, {dat}){
        println("I'm working!")
    }.consume {  }

    worker.requestTermination()
}

var glob = SomeData("asdf")
fun getAndClearGlob():SomeData{
    val temp = glob
    glob = SomeData("qwert")
    return temp
}

fun globalReference(){
    val worker = startWorker()

    worker.schedule(TransferMode.CHECKED, {getAndClearGlob()}){
        println("I'm working!")
    }.consume {  }

    worker.requestTermination()
}

fun freezeLocal(){
    val worker = startWorker()

    val localData = SomeData("asdf 👍")

    val future = worker.schedule(TransferMode.UNCHECKED, { localData }) {
        for (i in 0..1000000) {
            if(i%10000 == 0)
            println("In thread ${it.a}/$i")
        }
    }
    println("In main ${localData.a}")

    future.consume {  }

    println("In main ${localData.a}")

    worker.requestTermination()
}

class SomeData(val a:String)


fun twoWorkers(){
    val a = startWorker().freeze()
    val b = startWorker().freeze()

    a.schedule(TransferMode.CHECKED, {b}){

    }
}