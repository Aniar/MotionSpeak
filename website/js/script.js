$(document).ready(function() {
  $("#content").hide();
  $("#top").hide();
  $("#nav").hide();
    localStorage.clear();
    localStorage["test"] = "work?";

    $("#submitButton").click(function(){
      $("#intro").hide();
      $("#top").show();
      $("#content").show();
      $("#nav").show();
      $("#transcription").show();
      $.getJSON('data.json', function(data){
        var yVal = [];
        var xVal = [];
         var output="<ul>";

         // for (var i in data.try1) {
         //   output += "<li>" + data.try1[i]['FIELD1'] + " " + data.try1[i]['FIELD2'] + "</li>"
         // }
         for (var i in data.try1) {
           xVal[i] = data.try1[i]['FIELD1']
           yVal[i] = data.try1[i]['FIELD2']
         }
         // output+= "</ul>"
         var data2 = [
            {
              x: xVal,
              y: yVal,
              type: 'scatter'
            }
          ];
          $("#top").html("<h2>Session Results: </h2><hr>");
          $("#content").html(Plotly.newPlot('content', data2));
          $("#transcription").html("<h3>Transcription of Speech:</h3>\
            <p>The University of Rochester (commonly referred to as U of R or UR) is a private, nonsectarian, research university in Rochester, New York. The university grants undergraduate and graduate degrees, including doctoral and professional degrees. The university has six schools and various interdisciplinary programs.The University of Rochester is particularly noted for its Eastman School of Music. The university is also home to the Institute of Optics, founded in 1929, the first educational program in the US devoted exclusively to optics. Rochester's Laboratory for Laser Energetics is home to the second most energetic fusion laser in the world. In its history, five university alumni, two faculty, and one senior research associate at Strong Memorial Hospital have been awarded a Nobel Prize; eight alumni and four faculty members have won a Pulitzer Prize, and 19 faculty members have been awarded a Guggenheim Fellowship. Faculty and alumni of Rochester make up nearly one-quarter of the scientists on the board advising NASA in the development of the James Webb Space Telescope, which is scheduled to replace the Hubble Space Telescope in 2018.[citation needed] The departments of political science and economics have made a significant and consistent impact on positivist social science since the 1960s; the distinctive, mathematical approach pioneered at Rochester and closely affiliated departments is known as the Rochester school, and Rochester graduates and former affiliates are highly represented at faculties across top economics and political science departments.The University of Rochester, across all of its schools and campuses, enrolls approximately 5,600 undergraduates and 4,600 graduate students. Its 158 buildings house over 200 academic majors. Additionally, Rochester (along with its affiliated Strong Health System) is the largest employer in the Greater Rochester area and the sixth largest employer in New York.</p>");
        
      });
    });
  $("#log").click(function(){
    $.ajax({      
      success: function(){
        $("top").html("");
        $("#content").html("");
        $("#intro").show();
        $("#content").hide();
        $("#transcription").hide();
        $("#nav").hide();
        $("#top").hide();
      }
    })
  });
  });