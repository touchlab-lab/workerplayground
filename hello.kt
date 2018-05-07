import konan.worker.TransferMode
import konan.worker.freeze
import konan.worker.startWorker
import platform.darwin.glob

fun main(args: Array<String>) {
    globalReference()
}

fun simpleWorker(){
    val worker = startWorker()

    worker.schedule(TransferMode.CHECKED, {}){
        println("I'm working!")
    }.consume {  }

    worker.requestTermination()
}

/*val worker = startWorker()

fun doNotDoThis(){
    worker.schedule(TransferMode.CHECKED, {}){
        for(i in 0..1000000)
        {
            //Let's kill time
            val b = (i+i).toDouble()/1000.toDouble()
        }
    }.consume {  }

    worker.requestTermination()
}*/

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

    val localData = SomeData("asdf")
    localData.freeze()

    val future = worker.schedule(TransferMode.CHECKED, { localData }) {
        println("In thread ${it.a}")
    }
    println("In main ${localData.a}")

    future.consume {  }

    println("In main ${localData.a}")

    worker.requestTermination()
}

fun uncheckedLocal(){
    val worker = startWorker()

    val localData = SomeData("asdf")

    val future = worker.schedule(TransferMode.UNCHECKED, { localData }) {
        println("In thread ${it.a}")
    }
    println("In main ${localData.a}")

    future.consume {  }

    println("In main ${localData.a}")

    worker.requestTermination()
}

class SomeData(val a:String){
    init {
        println("init SomeData $a")
    }
}


fun twoWorkers(){
    val a = startWorker().freeze()
    val b = startWorker().freeze()

    a.schedule(TransferMode.CHECKED, {arrayOf(a, b)}){
        it[1].schedule(TransferMode.CHECKED, {it[0]}){
            print("Hello!")
        }
    }.consume {  }
    a.requestTermination()
    b.requestTermination()
}
