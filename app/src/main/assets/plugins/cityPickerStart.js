/**
 *
 * cityPicker.js
 * cityPicker is made by Framework picker component which effects like iOS native select.
 *
 * nzb329@163.com
 *
 * 2017-10-28
 *
 */
;
(function (window, $, undefined) {

  /**
   * [getProvince 获取省]
   * @param  {[Object]} regions [省市区数据]
   * @return {[Array]}          [省数组]
   */
  function getProvince(regions) {

    return regions['provincesArr'];
  }

  /**
   * [getCity 获取市]
   * @param  {[Object]} regions      [省市区数据]
   * @param  {[String]} provinceName [省名]
   * @return {[Array]}               [市数组]
   */
  function getCity(regions, provinceName) {

    return regions['provinces'][provinceName]['citiesArr'];
  }

  /**
   * [getArea 获取区]
   * @param  {[Object]} regions      [省市区数据]
   * @param  {[String]} provinceName [省名]
   * @param  {[String]} cityName     [市名]
   * @return {[Array]}               [区数组]
   */
  function getArea(regions, provinceName, cityName) {

    return regions['provinces'][provinceName]['cities'][cityName]['areasArr'];

  }

  // 初始化省市区
  var province = getProvince(regions),
    city = getCity(regions, '北京市'),
    area = getArea(regions, '北京市', '北京市');

  // 保存 picker 选择的省
  var provinceSelect = '';

  // 省市区联动 / Framework7 picker
  var pickerLocation = app.picker.create({
    inputEl: '#locationStart',
    rotateEffect: true,
    toolbarCloseText: '确定',
    cols: [{
      cssClass: 'f-s-14',
      width: '33.33%',
      textAlign: 'left',
      values: province,
      onChange: function (picker, province) {
        if (picker.cols[1].replaceValues) {
          provinceSelect = province;
          city = getCity(regions, province);
          area = getArea(regions, province, city[0]);
          picker.cols[1].replaceValues(city);
          picker.cols[2].replaceValues(area);
        }
      }
    },
    {
      cssClass: 'f-s-14',
      width: '33.33%',
      textAlign: 'center',
      values: city,
      onChange: function (picker, city) {
        if (picker.cols[2].replaceValues) {
          area = getArea(regions, provinceSelect, city);
          picker.cols[2].replaceValues(area);
        }
      }
    },
    {
      cssClass: 'f-s-14',
      width: '33.33%',
      textAlign: 'right',
      values: area,
    }
    ]
  });


})(window, {});
