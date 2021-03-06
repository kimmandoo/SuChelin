package Guide.suchelin.List

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Guide.suchelin.ContactActivity
import com.Guide.suchelin.DataClass.StoreDataScoreClass
import com.Guide.suchelin.List.RvAdapter
import com.Guide.suchelin.R
import com.Guide.suchelin.StoreDetail.StoreDetailActivity
import com.Guide.suchelin.config.BaseFragment
import com.Guide.suchelin.config.MyApplication
import com.Guide.suchelin.databinding.FragmentListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ListFragment : BaseFragment<FragmentListBinding>(
    FragmentListBinding::bind,
    R.layout.fragment_list
) {
    companion object {
        private const val FILTER_NAME = 1
        private const val FILTER_GRADE = 2
        private const val FILTER_NEW = 3
    }

    // 그림자 효과
    private var scrolledY = 0
    // list item
    private var items = ArrayList<StoreDataScoreClass>()
    // job
    private var job : Job? = null
    private var rvAdapter : RvAdapter? = null
    private var finishFlag = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로딩중 보여주기
        changeLoadingContent(true)

        // 초기화
        init()

        //고객센터
        binding.listIvContact.setOnClickListener {
            val intent = Intent(context, ContactActivity::class.java)
            startActivity(intent)
        }
        //info
        binding.listIvInfo.setOnClickListener {
            //dialog
            val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null)
            val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)
                .setTitle("앱소개")
            val mAlertDialog = mBuilder.show()
        }
    }

    private fun init() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if(MyApplication.dataControl.initFlag()){
                job = CoroutineScope(Dispatchers.Main).launch {
                    // adapter 설정
                    setAdapter(MyApplication.dataControl.allScores)

                    // 필터 설정
                    setFilter()

                    // loading 없애기
                    changeLoadingContent(false)
                }
            } else {
                Log.d("dataControl", "init 실행됨")
                if(!finishFlag) init()
            }
        }, 300)
    }

    private fun setAdapter(allScores: HashMap<String, Long>) {
        items = MyApplication.dataControl.getStoreDataScoreList(MyApplication.ApplicationContext(), allScores)

        items.apply {
            sortBy { it.score }
            reverse()
        }
        val topThreeId = ArrayList<Int>()
        topThreeId.add(items[0].id)
        topThreeId.add(items[1].id)
        topThreeId.add(items[2].id)

        items.sortBy { it.name }

        rvAdapter = RvAdapter(context, items, topThreeId)

        rvAdapter?.itemClick = object : RvAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                val intent = Intent(context, StoreDetailActivity::class.java)
                intent.putExtra("StoreName", items[position].name)
                intent.putExtra("StoreId", items[position].id)
                intent.putExtra("score", items[position].score)
                intent.putExtra("latitude", items[position].latitude)
                intent.putExtra("longitude", items[position].longitude)
                startActivity(intent)
            }
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrolledY += dy

                if (scrolledY > 5 && binding.listShadowView.visibility == View.GONE) {
                    binding.listShadowView.visibility = View.VISIBLE
                } else if (scrolledY < 5 && binding.listShadowView.visibility == View.VISIBLE) {
                    binding.listShadowView.visibility = View.GONE
                }
            }
        })
    }

    private fun changeLoadingContent(flag: Boolean) {
        if(flag){
            // loading 띄우기
            binding.listContentParent.visibility = View.GONE
            binding.listLoadingParent.visibility = View.VISIBLE
        } else {
            // content 내용 보여주기
            binding.listContentParent.visibility = View.VISIBLE
            binding.listLoadingParent.visibility = View.GONE
        }
    }

    private fun setFilter() {
        binding.listFilterNameTextView.setOnClickListener {
            // 필터 바꾸기
            changeFilter(FILTER_NAME)

            items.sortBy {
                it.name
            }
            // Log.d("score storedfilter name: ", allScores.toString())
            rvAdapter?.refreshItems(items)
        }
        binding.listFilterDistanceTextView.setOnClickListener {
            // 필터 바꾸기
            changeFilter(FILTER_NEW)
            //신규니까 id가 큰게 나중에 추가된 가게
            items.apply {
                sortBy {
                    it.id
                }
                reverse()
            }
            // Log.d("score storedfilter new: ", allScores.toString())

            rvAdapter?.refreshItems(items)
        }

        binding.listFilterGradeTextView.setOnClickListener {
            // 필터 바꾸기
            changeFilter(FILTER_GRADE)

            items.apply {
                sortBy { it.score }
                reverse()
            }

            val topThreeId = ArrayList<Int>()
            topThreeId.add(items[0].id)
            topThreeId.add(items[1].id)
            topThreeId.add(items[2].id)

            rvAdapter?.refresh(items, topThreeId)
        }
    }

    private fun changeFilter(filter: Int) {
        when (filter) {
            FILTER_NAME -> {
                binding.listFilterGradeTextView.setBackgroundResource(R.drawable.filter_uncheck_box)
                binding.listFilterNameTextView.setBackgroundResource(R.drawable.filter_check_box)
                binding.listFilterDistanceTextView.setBackgroundResource(R.drawable.filter_uncheck_box)
            }
            FILTER_GRADE -> {
                binding.listFilterGradeTextView.setBackgroundResource(R.drawable.filter_check_box)
                binding.listFilterNameTextView.setBackgroundResource(R.drawable.filter_uncheck_box)
                binding.listFilterDistanceTextView.setBackgroundResource(R.drawable.filter_uncheck_box)
            }
            FILTER_NEW -> {
                binding.listFilterGradeTextView.setBackgroundResource(R.drawable.filter_uncheck_box)
                binding.listFilterNameTextView.setBackgroundResource(R.drawable.filter_uncheck_box)
                binding.listFilterDistanceTextView.setBackgroundResource(R.drawable.filter_check_box)
            }
        }

        scrolledY = 0
    }

    override fun onDestroy() {
        job?.cancel()
        finishFlag = true
        super.onDestroy()
    }
}