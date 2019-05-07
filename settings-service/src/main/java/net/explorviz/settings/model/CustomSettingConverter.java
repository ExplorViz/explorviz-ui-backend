package net.explorviz.settings.model;

import com.mongodb.BasicDBObject;
import xyz.morphia.converters.SimpleValueConverter;
import xyz.morphia.converters.TypeConverter;
import xyz.morphia.mapping.MappedField;

public class CustomSettingConverter extends TypeConverter implements SimpleValueConverter {


  public CustomSettingConverter() {
    super(CustomSetting.class);
  }

  @Override
  public Object decode(final Class<?> targetClass, final Object fromDBObject,
      final MappedField optionalExtraInfo) {

    System.out.println("UIASDIUAHSDIUASHDI");

    final BasicDBObject basicDBO = (BasicDBObject) fromDBObject;
    final BasicDBObject idDBO = (BasicDBObject) basicDBO.get("_id");

    final String uId = idDBO.getString("userId");
    final String sId = idDBO.getString("settingId");

    final Object value = basicDBO.get("value");

    return new CustomSetting(uId, sId, value);


  }



}
