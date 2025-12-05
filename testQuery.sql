use testdb;

select launch_date,
count(*) as mission_count
from moon_mission
group by launch_date;

select year(m.launch_date) as year
from moon_mission m
group by year;

-- Räknar antal moon mission som utfördes respektive år
select year(m.launch_date) as year,
count(year(m.launch_date)) as mission_launched
from moon_mission m
group by year
order by year;





